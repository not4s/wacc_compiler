package symbolTable

import utils.SemanticException
import waccType.WAny
import waccType.WInt
import waccType.typesAreEqual

class PointerSymbolTable private constructor(
    private val inheritedEntries: Map<String, SymbolTableEntry>?,
    isGlobal: Boolean
) :
    SymbolTable(isGlobal) {
    constructor() : this(null, true)

    private val table = mutableMapOf<String, SymbolTableEntry>()


    override fun get(symbol: String): WAny {
        // Try to look this up in own table first.
        val ownEntry = table[symbol]
        if (ownEntry != null) {
            return ownEntry.value
        }
        // If not found, look this up in inherited entries.
        val parentEntry = inheritedEntries?.get(symbol)
        if (parentEntry != null) {
            return parentEntry.value
        }

        throw SemanticException("Attempted to .get() an undeclared variable: $symbol")
    }

    override fun declare(symbol: String, value: WAny) {
        // Make sure not re-declaring.
        if (symbol in table) {
            throw SemanticException("Attempted to redeclare value: $symbol")
        }
        table[symbol] = SymbolTableEntry(value)
    }

    override fun reassign(symbol: String, value: WAny) {
        // If own entry is found, check if the types match.
        val ownEntry = table[symbol]
        if (ownEntry != null) {
            if (typesAreEqual(value, ownEntry.value)) {
                // Reassign
                ownEntry.value = value
                return
            } else {
                throw SemanticException("Attempted to reassign $symbol(${ownEntry.value}) to different type: ${value.javaClass}.")
            }
        }
        // If not found, look this up in inherited entries.
        val parentEntry = inheritedEntries?.get(symbol)
        if (parentEntry != null) {
            if (typesAreEqual(value, parentEntry.value)) {
                // Reassign
                parentEntry.value = value
                return
            } else {
                throw SemanticException("Attempted to reassign outer-scope variable $symbol(${parentEntry.value}) to different type: ${value.javaClass}.")
            }
        }
        throw SemanticException("Attempted to reassign undeclared variable $symbol")
    }

    override fun reassign(arrSym: String, indices: Array<WInt>, value: WAny) {
        TODO("Not yet implemented")
    }


    override fun createChildScope(): PointerSymbolTable {
        val childInitialTable = mutableMapOf<String, SymbolTableEntry>()
        // Add all the grandparent entries
        inheritedEntries?.forEach { (k, v) -> childInitialTable[k] = v }
        // Add all parent entries, these will overwrite grandparent entries.
        table.forEach { (k, v) -> childInitialTable[k] = v }
        // Convert to immutable map to make sure no entries are being added.
        return PointerSymbolTable(childInitialTable.toMap(), false)
    }

    override fun toString(): String {
        return "PointerSymbolTable(inheritedEntries=$inheritedEntries, table=$table)"
    }
}

private class SymbolTableEntry(var value: WAny) {
    override fun toString(): String {
        return "STE(${value::class.simpleName} $value)"
    }
}