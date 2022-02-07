package symbolTable

import utils.SemanticException
import waccType.WAny
import waccType.WArray
import waccType.WInt
import waccType.typesAreEqual

class ParentRefSymbolTable private constructor(private val parentTable: ParentRefSymbolTable?) :
    SymbolTable() {
    constructor() : this(null)

    val dict = mutableMapOf<String, WAny>()

    override fun get(symbol: String): WAny {
        // Flashbacks to Haskell's >>=
        return dict[symbol] ?: parentTable?.get(symbol)
        ?: throw SemanticException("Attempted to get undeclared variable $symbol")
    }

    override fun declare(symbol: String, value: WAny) {
        if (dict.putIfAbsent(symbol, value) != null) {
            throw SemanticException("Attempted to redeclare variable $symbol")
        }
    }

    override fun reassign(symbol: String, value: WAny) {
        val prev = dict[symbol]
        // Check value exists
        if (prev != null) {
            // Then make sure it's the same type.
            if (typesAreEqual(prev, value)) {
                dict[symbol] = prev
                return
            } else {
                throw SemanticException("Attempted to reassign type of declared $prev to $value")
            }
        } else {
            // Doesn't exist, so check parent
            if (parentTable == null) {
                throw SemanticException("Attempted to reassign undeclared variable.")
            } else {
                parentTable.reassign(symbol, value)
            }
        }
    }

    override fun reassign(arrSym: String, indices: Array<WInt>, value: WAny) {
        val prev = dict[arrSym]
        // Make sure this is array.
        if (prev != null) {
            if (prev !is WArray) {
                throw SemanticException("Cannot access elements of non-array type: $prev")
            } else {
                // TODO("Implement this")
            }
        } else {
            if (parentTable == null) {
                throw SemanticException("Attempted to reassign undeclared variable.")
            } else {
                parentTable.reassign(arrSym, indices, value)
            }
        }
    }

    override fun isGlobal(): Boolean {
        return parentTable == null
    }

    override fun createChildScope(): SymbolTable {
        return ParentRefSymbolTable(this)
    }

    override fun toString(): String {
        return "${this.hashCode().toString(16)}, $dict, parent:${parentTable?.hashCode()?.toString(16)}"
    }

}

