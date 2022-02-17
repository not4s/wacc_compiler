package symbolTable

import semantic.SemanticChecker
import utils.SemanticErrorMessageBuilder
import waccType.WAny
import waccType.WArray
import waccType.WInt
import waccType.WUnknown
import waccType.WPair
import waccType.WPairKW

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

    /**
     * Goes through all the "layers" of an array with arbitrary number of dimensions
     * until it reaches non-array element type. It ensures that
     * arbitrary nested array contains arrays and only the internal array is the array of non-arrays.
     */
    private fun arrayTypeChecking(prev: WAny, indices: Array<WInt>, errBuilder: SemanticErrorMessageBuilder): WAny {
        var curr: WAny = prev
        repeat(indices.size) {
            SemanticChecker.checkThatTheValueIsWArray(curr, errBuilder)
            val safeCurr = curr as WArray
            curr = safeCurr.elemType
        }
        return curr
    }

    override fun get(symbol: String, errBuilder: SemanticErrorMessageBuilder): WAny {
        val valueGot = dict[symbol] ?: parentTable?.get(symbol, errBuilder)
        SemanticChecker.checkIfTheVariableIsInScope(valueGot, symbol, errBuilder)
        return valueGot
            ?: throw Exception("Semantic checker didn't throw SemanticException on null value of the symbol")
    }

    override fun get(arrSym: String, indices: Array<WInt>, errBuilder: SemanticErrorMessageBuilder): WAny {
        val prev = dict[arrSym]

        if (prev == null) {
            SemanticChecker.checkParentTableIsNotNull(parentTable, arrSym, errBuilder)
            return parentTable?.get(arrSym, indices, errBuilder)
                ?: throw Exception("Semantic checker failed to detect null parent table")
        }
        return arrayTypeChecking(prev, indices, errBuilder)
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

        if (prev == null) {
            SemanticChecker.checkParentTableIsNotNull(parentTable, symbol, errBuilder)
            parentTable ?: throw Exception("SemanticChecker failed to detect null parent table")
            parentTable.reassign(symbol, value, errBuilder)
            return
        }
        SemanticChecker.checkThatAssignmentTypesMatch(prev, value, errBuilder,
            failMessage = "Attempted to reassign type of declared $prev to $value"
        )
        dict[symbol] = value
    }

    override fun reassign(arrSym: String, indices: Array<WInt>, value: WAny, errBuilder: SemanticErrorMessageBuilder) {
        val prev = dict[arrSym]

        if (prev == null) {
            SemanticChecker.checkParentTableIsNotNull(parentTable, arrSym, errBuilder)
            parentTable?.reassign(arrSym, indices, value, errBuilder)
            return
        }
        val arrayType: WAny = arrayTypeChecking(prev, indices, errBuilder)
        SemanticChecker.checkThatAssignmentTypesMatch(arrayType, value, errBuilder)
    }

    override fun reassign(pairSym: String, fst: Boolean, value: WAny, errBuilder: SemanticErrorMessageBuilder) {
        val prev = dict[pairSym]
        if (prev is WPairKW) {
            dict[pairSym] = if (fst) WPair(value, WUnknown()) else WPair(WUnknown(), value)
            return
        }
        if (prev == null) {
            SemanticChecker.checkParentTableIsNotNull(parentTable, pairSym, errBuilder)
            parentTable?.reassign(pairSym, fst, value, errBuilder)
            return
        }
        SemanticChecker.checkThatTheValueIsPair(prev, fst, errBuilder)
        prev as WPair
        val elemT: WAny = if (fst) prev.leftType else prev.rightType
        SemanticChecker.checkThatAssignmentTypesMatch(elemT, value, errBuilder)
    }

    override fun createChildScope(): SymbolTable {
        return ParentRefSymbolTable(this, false, srcFilePath)
    }

    override fun toString(): String {
        val radix = 16
        return "${this.hashCode().toString(radix)}, $dict, parent:${
            parentTable?.hashCode()?.toString(radix)
        }, ${parentTable.toString()}"
    }
}
