package symbolTable

import utils.SemanticErrorMessageBuilder
import waccType.WAny
import utils.SemanticException
import waccType.WInt

abstract class SymbolTable(
    var isGlobal : Boolean,
    val srcFilePath: String
) {
    abstract fun get(symbol: String, errBuilder: SemanticErrorMessageBuilder): WAny

    abstract fun get(arrSym: String, indices: Array<WInt>, errBuilder: SemanticErrorMessageBuilder): WAny

    abstract fun getMap() : Map<String, WAny>

    inline fun <reified T : WAny> getAndCast(symbol: String, errBuilder: SemanticErrorMessageBuilder) : T {
        val value = this.get(symbol, errBuilder)
        if (value is T) {
            return value
        } else {
            throw SemanticException("Attempted to cast variable $value(${value::class.simpleName}) to ${T::class.simpleName}")
        }
    }

    abstract fun declare(symbol: String, value: WAny, errBuilder: SemanticErrorMessageBuilder)

    // Base type
    abstract fun reassign(symbol: String, value: WAny, errBuilder: SemanticErrorMessageBuilder)
    // Arrays
    abstract fun reassign(arrSym: String, indices: Array<WInt>, value: WAny, errBuilder: SemanticErrorMessageBuilder)
    // Pairs
    abstract fun reassign(pairSym: String, fst: Boolean, value: WAny, errBuilder: SemanticErrorMessageBuilder)
    abstract fun createChildScope(): SymbolTable
}