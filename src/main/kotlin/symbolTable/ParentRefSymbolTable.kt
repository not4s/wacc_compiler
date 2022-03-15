package symbolTable

import ast.Expr
import ast.WACCStruct
import codegen.ExprVisitor
import codegen.FunctionPool
import codegen.RegisterProvider
import codegen.WORD_SIZE
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*
import semantic.SemanticChecker
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.*

class ParentRefSymbolTable(
    private val parentTable: ParentRefSymbolTable?,
    isGlobal: Boolean,
    srcFilePath: String,
) : SymbolTable(
    isGlobal = isGlobal,
    srcFilePath = srcFilePath
) {
    constructor(srcFilePath: String) : this(null, true, srcFilePath)

    private val dict = mutableMapOf<String, WAny>()
    val redeclaredVars = mutableSetOf<String>()
    var forceOffset = 0

    /**
     * Goes through all the "layers" of an array with arbitrary number of dimensions
     * until it reaches non-array element type. It ensures that
     * arbitrary nested array contains arrays and only the internal array is the array of non-arrays.
     */
    private fun arrayTypeChecking(
        prev: WAny,
        indices: Array<WInt>,
        errBuilder: SemanticErrorMessageBuilder,
    ): WAny {
        var curr: WAny = prev
        repeat(indices.size) {
            SemanticChecker.checkThatTheValueIsWArray(curr, errBuilder)
            val safeCurr = curr as WArray
            curr = safeCurr.elemType
        }
        return curr
    }

    override fun get(symbol: String, errorMessageBuilder: SemanticErrorMessageBuilder): WAny {
        val valueGot = dict[symbol] ?: parentTable?.get(symbol, errorMessageBuilder)
        SemanticChecker.checkIfTheVariableIsInScope(valueGot, symbol, errorMessageBuilder)
        return valueGot
            ?: throw Exception("Semantic checker didn't throw SemanticException on null value of the symbol")
    }

    override fun get(
        arrSym: String,
        indices: Array<WInt>,
        errorMessageBuilder: SemanticErrorMessageBuilder,
    ): WAny {
        val prev = dict[arrSym]
        if (prev == null) {
            SemanticChecker.checkParentTableIsNotNull(parentTable, arrSym, errorMessageBuilder)
            return parentTable?.get(arrSym, indices, errorMessageBuilder)
                ?: throw Exception("Semantic checker failed to detect null parent table")
        }
        return arrayTypeChecking(prev, indices, errorMessageBuilder)
    }

    override fun getStructElemType(
        structIdent: String,
        structElems: List<String>,
        errorMessageBuilder: SemanticErrorMessageBuilder
    ): WAny {
        // is the struct identifier in the scope?
        val structType = get(structIdent, errorMessageBuilder)
        if (structType !is WACCStruct) {
            throw Exception("Expected type WACCStruct but got ${structType::class} instead")
        }
        // is the elem a part of the struct?
        var terminalType = structType
        for (i in structElems.indices) {
            if ((terminalType as WACCStruct).params.containsKey(structElems[i])) {
                if (i == structElems.size - 1) {
                    // if this is the last element, then it is the terminal node, no need to get
                    return terminalType.params[structElems[i]]!!
                } else {
                    // if this isn't the last element, then it HAS to be a struct, and therefore
                    // we must get the original WACC struct definition in the symbol table
                    terminalType = get(
                        (terminalType.params[structElems[i]]!! as WStruct).identifier,
                        errorMessageBuilder
                    )
                }
            } else {
                errorMessageBuilder.elementDoesntExistInStruct(
                    structIdent,
                    structElems.subList(0, i + 1).reduce { a, b -> "$a.$b" }).buildAndPrint()
                throw SemanticException("Element doesn't exist in struct")
            }
        }
        throw Exception("Should not have reached this, is structElems > 1?")
    }

    override fun getMap(): Map<String, WAny> {
        return dict
    }

    override fun declare(
        symbol: String,
        value: WAny,
        errorMessageBuilder: SemanticErrorMessageBuilder,
    ) {
        val prev = dict.putIfAbsent(symbol, value)
        SemanticChecker.checkIfRedeclarationHappens(prev, symbol, errorMessageBuilder)
    }

    override fun reassign(
        symbol: String,
        value: WAny,
        errorMessageBuilder: SemanticErrorMessageBuilder,
    ) {
        val prev = dict[symbol]

        if (prev == null) {
            SemanticChecker.checkParentTableIsNotNull(parentTable, symbol, errorMessageBuilder)
            parentTable ?: throw Exception("SemanticChecker failed to detect null parent table")
            parentTable.reassign(symbol, value, errorMessageBuilder)
            return
        }

        SemanticChecker.checkThatAssignmentTypesMatch(
            prev, value, errorMessageBuilder,
            failMessage = "Attempted to reassign type of declared $prev to $value"
        )
        dict[symbol] = value
    }

    override fun reassign(
        arrSym: String,
        indices: Array<WInt>,
        value: WAny,
        errorMessageBuilder: SemanticErrorMessageBuilder,
    ) {
        val prev = dict[arrSym]

        if (prev == null) {
            SemanticChecker.checkParentTableIsNotNull(parentTable, arrSym, errorMessageBuilder)
            parentTable?.reassign(arrSym, indices, value, errorMessageBuilder)
            return
        }
        val arrayType: WAny = arrayTypeChecking(prev, indices, errorMessageBuilder)
        SemanticChecker.checkThatAssignmentTypesMatch(arrayType, value, errorMessageBuilder)
    }

    override fun reassign(
        pairSym: String,
        fst: Boolean,
        value: WAny,
        errorMessageBuilder: SemanticErrorMessageBuilder,
    ) {
        val prev = dict[pairSym]
        if (prev is WPairKW) {
            dict[pairSym] = if (fst) WPair(value, WUnknown) else WPair(WUnknown, value)
            return
        }
        if (prev == null) {
            SemanticChecker.checkParentTableIsNotNull(parentTable, pairSym, errorMessageBuilder)
            parentTable?.reassign(pairSym, fst, value, errorMessageBuilder)
            return
        }
        SemanticChecker.checkThatTheValueIsPair(prev, fst, errorMessageBuilder)
        prev as WPair
        val elemT: WAny = if (fst) prev.leftType else prev.rightType
        SemanticChecker.checkThatAssignmentTypesMatch(elemT, value, errorMessageBuilder)
    }

    override fun createChildScope(): SymbolTable {
        return ParentRefSymbolTable(this, false, srcFilePath)
    }

    override fun asmAssign(
        symbol: String,
        fromRegister: Register,
        data: DataDeclaration,
        type: WAny? // null if assigning, type if declaring.
    ): List<WInstruction> {
        // Work out this variable's offset from the start of symbol table.
        var offset = -data.spOffset + forceOffset
        var isSmall = false
        if (symbol in getMap()) {
            for ((k, v) in getMap().entries) {
                offset += typeToByteSize(v)
                if (k == symbol) {
                    isSmall = v is WBool || v is WChar
                    break
                }
            }
            if (type == null) {
                if (symbol in redeclaredVars) {
                    // Assigning, variable redeclare.
                    return listOf(
                        STR(
                            fromRegister,
                            Register.SP,
                            totalByteSize - offset,
                            isSignedByte = isSmall
                        )
                    )
                } else {
                    // Assigning, variable NOT redeclare. Go to parent.
                    data.spOffset += totalByteSize
                    try {
                        return parentTable?.asmAssign(symbol, fromRegister, data, type)!!
                    } finally {
                        data.spOffset -= totalByteSize
                    }
                }
            } else {
                if (symbol in redeclaredVars) {
                    // Declaring, variable redeclare.
                    throw Exception("Double declare")
                } else {
                    // Declaring, variable NOT redeclare.
                    redeclaredVars.add(symbol)
                    return listOf(
                        STR(
                            fromRegister,
                            Register.SP,
                            totalByteSize - offset,
                            isSignedByte = isSmall
                        )
                    )
                }
            }
        } else {
            // Not found in symbol table, go to parent.
            data.spOffset += totalByteSize
            try {
                return parentTable?.asmAssign(symbol, fromRegister, data, type)!!
            } finally {
                data.spOffset -= totalByteSize
            }
        }
    }

    override fun asmAssign(
        arrSym: String,
        indices: Array<Expr>,
        fromRegister: Register,
        data: DataDeclaration,
        rp: RegisterProvider,
        functionPool: FunctionPool
    ): List<WInstruction> {
        val isSmall = typeToByteSize(
            (get(
                arrSym,
                SemanticErrorMessageBuilder()
            ) as WArray).elemType
        ) != WORD_SIZE
        //    save the fromRegister somewhere somehow in-case it gets overwritten by any of
        //    the indices' evaluation operations
        val saveFromRegister = PUSH(fromRegister, data)
        //    translate expression(s) in the Array and for each store somewhere somehow
        //    whilst save the indices backwards on the stack
        val translatingExpressions = indices.reversed().map {
            ExprVisitor(data, rp, functionPool).visit(it).plus(PUSH(Register.R0, data))
        }.flatten()
        //    get the address in the heap according to the index
        val intermediateArrayLocationMagic: List<WInstruction> =
            asmGet(arrSym, Register.R4, data)

        //    Store the original fromRegister into the address calculated above
        val restoringIndices = indices.map { _ ->
            listOf(
                POP(Register.R0, data),
                B("p_check_array_bounds"),
                ADD(Register.R4, Register.R4, Immediate(4)),
                if (isSmall) {
                    ADD(Register.R4, Register.R4, Register.R0)
                } else {
                    ADD(Register.R4, Register.R4, LSLRegister(Register.R0, 2))
                }
            )
        }.flatten()

        return listOf(
            (saveFromRegister)
        )
            .asSequence()
            .plus(translatingExpressions)
            .plus(intermediateArrayLocationMagic)
            .plus(restoringIndices)
            .plus(POP(Register.R0, data))
            .plus(STR(Register.R0, Register.R4, isSignedByte = isSmall))
            .toList()
    }

    override fun asmAssign(
        structSym: String,
        elem: List<String>,
        fromRegister: Register,
        data: DataDeclaration,
        rp: RegisterProvider,
        functionPool: FunctionPool
    ): List<WInstruction> {
        // first working with immediate elements, not nested struct elements.
        val addressOfStructRegister = rp.get()
        val addressOfElemRegister = rp.get()
        // get the address of the struct from the stack.
        val addressOfStruct = asmGet(structSym, addressOfStructRegister, data)
        // calculate the position of the element(s) from the stack
        val offset = getOffset(get(structSym, SemanticErrorMessageBuilder()) as WACCStruct, elem[0])
        // add the offset into destination register, which will contain the value of the element
        val addressOfElem = ADD(addressOfElemRegister, addressOfStructRegister, Immediate(offset))
        val storeFromRegisterToElem = STR(fromRegister, addressOfElemRegister)
        rp.ret()
        rp.ret()
        return addressOfStruct.plus(addressOfElem).plus(storeFromRegisterToElem)
    }

    override fun asmGet(
        symbol: String,
        toRegister: Register,
        data: DataDeclaration
    ): List<WInstruction> {
        // Work out this variable's offset from the start of symbol table.
        var offset = -data.spOffset + forceOffset
        var isSmall = false
        if (symbol in getMap()) {
            for ((k, v) in getMap().entries) {
                offset += typeToByteSize(v)
                if (k == symbol) {
                    isSmall = v is WBool || v is WChar
                    break
                }
            }
            if (symbol in redeclaredVars) {
                // Redeclare in scope, return that
                return listOf(
                    LDR(
                        toRegister,
                        ImmediateOffset(
                            Register.SP,
                            totalByteSize - offset
                        ),
                        isSignedByte = isSmall
                    )
                )
            } else {
                // Not declared yet. Go to parent.
                data.spOffset += totalByteSize
                try {
                    return parentTable!!.asmGet(symbol, toRegister, data)
                } finally {
                    data.spOffset -= totalByteSize
                }
            }

        } else {
            // Not found in symbol table, go to parent.
            data.spOffset += totalByteSize
            try {
                return parentTable!!.asmGet(symbol, toRegister, data)
            } finally {
                data.spOffset -= totalByteSize
            }
        }
    }

    override fun asmGet(
        arrSym: String,
        indices: Array<Expr>,
        toRegister: Register,
        data: DataDeclaration,
        rp: RegisterProvider,
        functionPool: FunctionPool
    ): List<WInstruction> {
        val isSmall =
            typeToByteSize((get(arrSym, SemanticErrorMessageBuilder()) as WArray).elemType) != 4
        val translatingExpressions = indices.reversed().map {
            ExprVisitor(data, rp, functionPool).visit(it).plus(PUSH(Register.R0, data))
        }.flatten()
        //    get the address in the heap according to the index
        val intermediateArrayLocationMagic: List<WInstruction> =
            asmGet(arrSym, Register.R4, data)

        //    Store the original fromRegister into the address calculated above
        val restoringIndices = indices.map { _ ->
            listOf(
                POP(Register.R0, data),
                B("p_check_array_bounds"),
                ADD(Register.R4, Register.R4, Immediate(4)),
                if (isSmall) {
                    ADD(Register.R4, Register.R4, Register.R0)
                } else {
                    ADD(Register.R4, Register.R4, LSLRegister(Register.R0, 2))
                },
                LDR(Register.R4, Register.R4, isSignedByte = isSmall)
            )
        }.flatten()

        return listOf<WInstruction>(
        )
            .asSequence()
            .plus(translatingExpressions)
            .plus(intermediateArrayLocationMagic)
            .plus(restoringIndices)
            .plus(MOV(toRegister, Register.R4))
            .toList()
    }

    override fun asmGet(
        symbol: String,
        elem: List<String>,
        toRegister: Register,
        registerProvider: RegisterProvider,
        data: DataDeclaration,
        functionPool: FunctionPool
    ): List<WInstruction> {
        // first working with immediate elements, not nested struct elements.
        // get the address of the struct from the stack.
        val addressOfStruct = registerProvider.get()
        val getAddressOfStruct = asmGet(symbol, addressOfStruct, data)
        // calculate the position of the element(s) from the stack
        val offsetOfElement =
            getOffset(get(symbol, SemanticErrorMessageBuilder()) as WACCStruct, elem[0])
        // add the offset into destination register, which will contain the value of the element
        val addressOfElem = ADD(toRegister, addressOfStruct, Immediate(offsetOfElement))
        val valueOfElem = LDR(toRegister, toRegister)
        registerProvider.ret()
        return getAddressOfStruct.plus(addressOfElem).plus(valueOfElem)
    }


    private fun getOffset(
        struct: WACCStruct,
        elem: String
    ): Int {
        var offset = 0
        for (element in struct.params) {
            if (element.key == elem) {
                break;
            }
            offset += typeToByteSize(element.value)
        }
        return offset
    }


    override fun toString(): String {
        val radix = 16
        return "${this.hashCode().toString(radix)}, $dict, parent:${
            parentTable?.hashCode()?.toString(radix)
        }, ${parentTable.toString()}"
    }
}
