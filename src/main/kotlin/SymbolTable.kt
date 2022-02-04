class SymbolTable(initialTable: Map<String, SymbolTableEntry<Any>>? = null) {
    val table = mutableMapOf<String, SymbolTableEntry<Any>>()
    val parentTable = initialTable

    // Attempt to reassign the value of this variable.
    // Order:
    // - Try to reassign variable in own scope
    //     - Declared as correct type -> reassign
    //     - Declared as wrong type -> semantic error
    //     - Undeclared -> try parent scope
    //         - Declared as correct type -> reassign in parent
    //         - Declared as wrong type -> semantic error
    //         - Undeclared -> semantic error
    inline fun <reified T : Any> reassign(symbol: String, value: T) {
        if (symbol in table) {
            val prev = table[symbol]!!.value
            if (prev is T) {
                table[symbol]!!.value = value
                return
            } else {
                throw SemanticException("Attempted to mutate variable type (${prev::class.simpleName})$symbol=$prev to (${T::class.simpleName})$symbol=$value")
            }
        } else {
            if (parentTable != null) {
                if (symbol in parentTable) {
                    val prev = parentTable[symbol]!!.value
                    if (prev is T) {
                        parentTable[symbol]!!.value = value
                        return
                    } else {
                        throw SemanticException("Attempted to mutate outer-scope variable type (${prev::class.simpleName})$symbol=$prev to (${T::class.simpleName})$symbol=$value")
                    }
                }
            } else {
                throw SemanticException("Attempted to assign (${T::class.simpleName})$symbol=$value to undeclared variable $symbol")
            }
        }
    }

    // Attempt to declare a new variable
    // Order:
    // - Try to declare variable in own scope
    //     - Undeclared -> declare
    //     - Declared -> semantic error
    inline fun <reified T : Any> declare(symbol: String, value: T) {
        if (symbol !in table) {
            table[symbol] = SymbolTableEntry(value)
        } else {
            val prev = table[symbol]!!.value
            throw SemanticException("Attempted to redeclare variable (${prev::class.simpleName})$symbol=$prev to (${T::class.simpleName})$symbol=$value")
        }
    }

    // Attempt to get the value T of a variable
    // Order:
    // - Try to get variable in own scope
    //     - Declared as correct type -> return it
    //     - Declared as wrong type -> semantic error
    //     - Undeclared -> try parent scope
    //         - Declared as correct type -> return it
    //         - Declared as wrong type -> semantic error
    //         - Undeclared -> semantic error
    inline fun <reified T : Any> get(symbol: String): T {
        if (symbol in table) {
            val prev = table[symbol]!!.value
            if (prev is T) {
                return prev
            } else {
                throw SemanticException("Attempted to cast variable (${prev::class.simpleName})$symbol=$prev to ${T::class.simpleName}")
            }
        } else {
            if (parentTable != null) {
                if (symbol in parentTable) {
                    val prev = parentTable[symbol]!!.value
                    if (prev is T) {
                        return prev
                    } else {
                        throw SemanticException("Attempted to cast outer-scope variable (${prev::class.simpleName})$symbol=$prev to ${T::class.simpleName}")
                    }
                }
            }
            throw SemanticException("Attempted to access undeclared variable $symbol")
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