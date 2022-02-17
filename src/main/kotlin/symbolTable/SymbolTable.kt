package symbolTable

import utils.SemanticErrorMessageBuilder
import waccType.WAny
import utils.SemanticException
import waccType.WInt

abstract class SymbolTable(
    var isGlobal : Boolean,
    val srcFilePath: String
) {
    abstract fun get(symbol: String, errorMessageBuilder: SemanticErrorMessageBuilder): WAny

    abstract fun get(arrSym: String, indices: Array<WInt>, errorMessageBuilder: SemanticErrorMessageBuilder): WAny

    abstract fun getMap() : Map<String, WAny>

    inline fun <reified T : WAny> getAndCast(symbol: String, errorMessageBuilder: SemanticErrorMessageBuilder) : T {
        val value = this.get(symbol, errorMessageBuilder)
        if (value is T) {
            return value
        } else {
            throw SemanticException("Attempted to cast variable $value(${value::class.simpleName}) to ${T::class.simpleName}")
        }
    }

    abstract fun declare(symbol: String, value: WAny, errorMessageBuilder: SemanticErrorMessageBuilder)

    /**
     * Reassignment for base types
     */
    abstract fun reassign(symbol: String, value: WAny, errorMessageBuilder: SemanticErrorMessageBuilder)

    /**
     * Reassignment for arrays
     */
    abstract fun reassign(arrSym: String, indices: Array<WInt>, value: WAny, errorMessageBuilder: SemanticErrorMessageBuilder)

    /**
     * Reassignment for pairs
     */
    abstract fun reassign(pairSym: String, fst: Boolean, value: WAny, errorMessageBuilder: SemanticErrorMessageBuilder)

    abstract fun createChildScope(): SymbolTable
}