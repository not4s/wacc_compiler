package symbolTable

import ast.Expr
import codegen.ExprVisitor
import codegen.FunctionPool
import codegen.RegisterProvider
import instructions.WInstruction
import instructions.misc.DataDeclaration
import instructions.misc.ImmediateOffset
import instructions.misc.Label
import instructions.misc.Register
import instructions.operations.*
import semantic.SemanticChecker
import utils.SemanticErrorMessageBuilder
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
    private val redeclaredVars = mutableSetOf<String>()

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
            dict[pairSym] = if (fst) WPair(value, WUnknown()) else WPair(WUnknown(), value)
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
        var offset = -data.spOffset
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
                    // Assigning, variable redeclared.
                    return listOf(
                        STR(
                            fromRegister,
                            Register.stackPointer(),
                            totalByteSize - offset,
                            isSignedByte = isSmall
                        )
                    )
                } else {
                    // Assigning, variable NOT redeclared. Go to parent.
                    data.spOffset += totalByteSize
                    try {
                        return parentTable?.asmAssign(symbol, fromRegister, data, type)!!
                    } finally {
                        data.spOffset -= totalByteSize
                    }
                }
            } else {
                if (symbol in redeclaredVars) {
                    // Declaring, variable redeclared.
                    throw Exception("Double declare")
                } else {
                    // Declaring, variable NOT redeclared.
                    redeclaredVars.add(symbol)
                    return listOf(
                        STR(
                            fromRegister,
                            Register.stackPointer(),
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
        // STEPS:
        //    save the fromRegister somewhere somehow in-case it gets overwritten by any of
        //    the indices' evaluation operations
        val saveFromRegister = PUSH(fromRegister)
        //    translate expression(s) in the Array and for each store somewhere somehow
        //    whilst save the indices backwards on the stack
        val translatingExpressions = indices.map {
            ExprVisitor(data, rp, functionPool).visit(it).plus(PUSH(Register.resultRegister()))
        }.flatten()
        //    get the address in the heap according to the index
        val intermediateArrayLocationMagic: List<WInstruction> =
            asmGet(arrSym, Register.resultRegister(), data)

        //    Store the original fromRegister into the address calculated above
        val storeValueIntoElem = listOf(
            POP(fromRegister),
            STR(
                fromRegister,
                Register.resultRegister(),
                0,
                false
            )
        )

        return listOf(Label("# START OF ASM"))
            .plus(saveFromRegister)
            .plus(translatingExpressions)
            .plus(intermediateArrayLocationMagic)
            .plus(B("p_check_array_bounds", link=true))
            .plus(storeValueIntoElem)
            .plus(Label("# END OF ASM"))
    }

    override fun asmAssign(
        pairSym: String,
        fst: Boolean,
        fromRegister: Register,
        data: DataDeclaration,
    ): List<WInstruction> {
        TODO("Not yet implemented")
    }

    override fun asmGet(symbol: String, toRegister: Register, data: DataDeclaration): List<WInstruction> {
        // Work out this variable's offset from the start of symbol table.
        var offset = -data.spOffset
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
                // Redeclared in scope, return that
                return listOf(
                    LDR(
                        toRegister,
                        ImmediateOffset(
                            Register.stackPointer(),
                            totalByteSize - offset
                        ),
                        isSignedByte = isSmall
                    )
                )
            } else {
                // Not declared yet. Go to parent.
                data.spOffset += totalByteSize
                try {
                    return parentTable?.asmGet(symbol, toRegister, data)!!
                } finally {
                    data.spOffset -= totalByteSize
                }
            }

        } else {
            // Not found in symbol table, go to parent.
            data.spOffset += totalByteSize
            try {
                return parentTable?.asmGet(symbol, toRegister, data)!!
            } finally {
                data.spOffset -= totalByteSize
            }
        }
    }

    override fun toString(): String {
        val radix = 16
        return "${this.hashCode().toString(radix)}, $dict, parent:${
            parentTable?.hashCode()?.toString(radix)
        }, ${parentTable.toString()}"
    }
}
