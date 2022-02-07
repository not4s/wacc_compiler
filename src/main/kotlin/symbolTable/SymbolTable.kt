package symbolTable

import waccType.WAny
import utils.SemanticException
import waccType.WInt

abstract class SymbolTable(var isGlobal : Boolean) {

    abstract fun get(symbol: String): WAny

    abstract fun get(arrSym: String, indices: Array<WInt>): WAny

    inline fun <reified T : WAny> getAndCast(symbol: String) : T {
        val value = this.get(symbol)
        if (value is T) {
            return value
        } else {
            throw SemanticException("Attempted to cast variable $value(${value::class.simpleName}) to ${T::class.simpleName}")
        }
    }

    abstract fun declare(symbol: String, value: WAny)

    // Base type
    abstract fun reassign(symbol: String, value: WAny)
    // Arrays
    abstract fun reassign(arrSym: String, indices: Array<WInt>, value: WAny)
    // Pairs
    abstract fun reassign(pairSym: String, fst: Boolean, value: WAny)
    abstract fun createChildScope(): SymbolTable
}