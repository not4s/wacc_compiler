class SymbolTable() {
    private constructor(initialTable : MutableMap<String, SymbolTableEntry<Any>>) : this() {
        table.putAll(initialTable)
    }

    val table = mutableMapOf<String, SymbolTableEntry<Any>>()

    // Attempt to add this variable to the symbol table.
    // Throws relevant syntax error if this variable is of different type.
    inline fun <reified T : Any> put(symbol: String, value: T) {
        // Symbol already exists: overwrite or throw error
        if (symbol in table) {
            val prev = table[symbol]!!.value
            // If the type is being changed, throw semantic error.
            if (prev !is T) {
                throw SemanticException("Attempted to mutate variable (${prev::class.simpleName})$symbol to (${T::class.simpleName})$symbol")
            } else {
                // Types match, update value
                table[symbol]!!.value = value
            }
        } else {
            table[symbol] = SymbolTableEntry(value)
        }
    }

    inline fun <reified T: Any> get(symbol: String) : T {
        if (symbol !in table) {
            throw SemanticException("Attempted to refer to uninitialized variable $symbol")
        } else {
            val value = table[symbol]!!.value
            if (value is T) {
                return value
            } else {
                throw SemanticException("Attempted to cast variable (${value::class.simpleName})$symbol to ${T::class.simpleName}")
            }
        }
    }
}

class SymbolTableEntry<T : Any>(var value: T) {
    override fun toString(): String {
        return "(${value::class.simpleName})$value"
    }

}

class SemanticException(private val reason: String) : Exception() {
    override val message: String
        get() = "Semantic error!\n$reason"
}

fun main() {
    val test = SymbolTable()
    println(test.table)
    test.put("x", 2)
    println(test.table)
    test.put("y", 3)
    println(test.table)
    test.put("y", 4)
    println(test.table)

    println("Get y as int: ${test.get<Int>("y")}")
    println("Get y as string: ${test.get<String>("y")}")
}