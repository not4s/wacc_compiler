package semantic

interface SymbolTable<T> {

    fun get(symbol: String): T

    fun declare(symbol: String, value: T)

    fun reassign(symbol: String, value: T)

    fun createChildScope(): SymbolTable<T>
}