package symbolTable

import waccType.WAny
import utils.SemanticException
import waccType.WInt

abstract class SymbolTable(var isGlobal : Boolean) {

    abstract fun get(symbol: String): WAny

    inline fun <reified T : WAny> getAndCast(symbol: String) : T {
        val value = this.get(symbol)
        if (value is T) {
            return value
        } else {
            throw SemanticException("Attempted to cast variable $value(${value::class.simpleName}) to ${T::class.simpleName}")
        }
    }

    abstract fun declare(symbol: String, value: WAny)

    abstract fun reassign(symbol: String, value: WAny)
    abstract fun reassign(arrSym: String, indices: Array<WInt>, value: WAny)
    abstract fun createChildScope(): SymbolTable
}