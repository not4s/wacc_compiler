package symbolTable

import semantic.SemanticChecker
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.*

class ParentRefSymbolTable(
    private val parentTable: ParentRefSymbolTable?,
    isGlobal: Boolean,
    srcFilePath: String
) : SymbolTable(
    isGlobal = isGlobal,
    srcFilePath = srcFilePath
) {
    constructor(srcFilePath: String) : this(null, true, srcFilePath)

    private val dict = mutableMapOf<String, WAny>()

    override fun get(symbol: String, errBuilder: SemanticErrorMessageBuilder): WAny {
        val valueGot = dict[symbol] ?: parentTable?.get(symbol, errBuilder)
        SemanticChecker.checkIfTheVariableIsInScope(valueGot, symbol, errBuilder)
        return valueGot
            ?: throw Exception("Semantic checker didn't throw SemanticException on null value of the symbol")
    }

    override fun get(arrSym: String, indices: Array<WInt>, errBuilder: SemanticErrorMessageBuilder): WAny {
        val prev = dict[arrSym]
        // Make sure this is array.
        if (prev != null) {
            SemanticChecker
            if (prev !is WArray) {
                errBuilder.nonArrayTypeElemAccess(prev).buildAndPrint()
                throw SemanticException("Cannot access index elements of non-array type: $prev")
            } else {
                // 'Peel off' one layer of array per index.
                var curr: WAny = prev
                for (idx in indices) {
                    if (curr !is WArray) {
                        // Not array, but another index is requested?
                        errBuilder.nonArrayTypeElemAccess(curr).buildAndPrint()
                        throw SemanticException("Type $curr is not indexable.")
                    } else {
                        curr = curr.elemType
                    }
                }
                return curr
            }
        } else {
            SemanticChecker.checkParentTableIsNotNull(parentTable, arrSym, errBuilder)
            return parentTable?.get(arrSym, indices, errBuilder)
                ?: throw Exception("Semantic checker failed to detect null parent table")
        }
    }

    override fun getMap(): Map<String, WAny> {
        return dict
    }

    override fun declare(symbol: String, value: WAny, errBuilder: SemanticErrorMessageBuilder) {
        val prev = dict.putIfAbsent(symbol, value)
        SemanticChecker.checkIfRedeclarationHappens(prev, symbol, errBuilder)
    }

    override fun reassign(symbol: String, value: WAny, errBuilder: SemanticErrorMessageBuilder) {
        val prev = dict[symbol]
        // Check value exists
        if (prev != null) {
            // Then make sure it's the same type.
            if (typesAreEqual(prev, value)) {
                dict[symbol] = value
                return
            } else {
                errBuilder.assignmentTypeMismatch(prev, value).buildAndPrint()
                throw SemanticException("Attempted to reassign type of declared $prev to $value")
            }
        } else {
            SemanticChecker.checkParentTableIsNotNull(parentTable, symbol, errBuilder)
            parentTable?.reassign(symbol, value, errBuilder)
        }
    }

    override fun reassign(arrSym: String, indices: Array<WInt>, value: WAny, errBuilder: SemanticErrorMessageBuilder) {
        val prev = dict[arrSym]
        // Make sure this is array.
        if (prev != null) {
            if (prev !is WArray) {
                errBuilder.nonArrayTypeElemAccess(prev).buildAndPrint()
                throw SemanticException("Cannot access elements of non-array type: $prev")
            } else {
                // 'Peel off' one layer of array per index.
                var curr: WAny = prev
                for (idx in indices) {
                    if (curr !is WArray) {
                        // Not array, but another index is requested?
                        errBuilder.nonArrayTypeElemAccess(curr).buildAndPrint()
                        throw SemanticException("Type $curr is not indexable.")
                    } else {
                        curr = curr.elemType
                    }
                }
                // Then make sure it's the same type.
                if (typesAreEqual(curr, value)) {
                    // TODO: BACK-END: Reassign value
                    return
                } else {
                    errBuilder.assignmentTypeMismatch(prev, value).buildAndPrint()
                    throw SemanticException("Attempted to reassign type of declared $prev to $value")
                }
            }
        } else {
            SemanticChecker.checkParentTableIsNotNull(parentTable, arrSym, errBuilder)
            parentTable?.reassign(arrSym, indices, value, errBuilder)
        }
    }

    override fun reassign(pairSym: String, fst: Boolean, value: WAny, errBuilder: SemanticErrorMessageBuilder) {
        val prev = dict[pairSym]
        // Make sure this is a pair.
        if (prev != null) {
            if (prev is WPairKW) {
                val newType = if (fst) WPair(value, WUnknown()) else WPair(WUnknown(), value)
                dict[pairSym] = newType
                return
            }
            if (prev !is WPair) {
                errBuilder.unOpInvalidType(
                    prev, if (fst) {
                        "fst"
                    } else {
                        "snd"
                    }
                ).buildAndPrint()
                throw SemanticException("Cannot obtain (fst/snd) from type: $prev")
            } else {
                // Extract correct element
                val elemT: WAny = if (fst) prev.leftType else prev.rightType
                // Make sure matching type
                if (typesAreEqual(elemT, value)) {
                    // TODO: BACK-END: Reassign value
                    return
                } else {
                    errBuilder.assignmentTypeMismatch(prev, value).buildAndPrint()
                    throw SemanticException("Attempted to reassign type of declared $prev to $value")
                }
            }
        } else {
            SemanticChecker.checkParentTableIsNotNull(parentTable, pairSym, errBuilder)
            parentTable?.reassign(pairSym, fst, value, errBuilder)
        }
    }

    override fun createChildScope(): SymbolTable {
        return ParentRefSymbolTable(this, false, srcFilePath)
    }

    override fun toString(): String {
        return "${this.hashCode().toString(16)}, $dict, parent:${
            parentTable?.hashCode()?.toString(16)
        }, ${parentTable.toString()}"
    }

}
