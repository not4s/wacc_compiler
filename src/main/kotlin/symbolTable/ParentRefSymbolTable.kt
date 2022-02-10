package symbolTable

import utils.SemanticException
import waccType.*

class ParentRefSymbolTable(private val parentTable: ParentRefSymbolTable?, isGlobal: Boolean) :
    SymbolTable(isGlobal) {
    constructor() : this(null, true)

    private val dict = mutableMapOf<String, WAny>()

    override fun get(symbol: String): WAny {
        // Flashbacks to Haskell's >>=
        return dict[symbol] ?: parentTable?.get(symbol)
        ?: throw SemanticException("Attempted to get undeclared variable $symbol")
    }

    override fun get(arrSym: String, indices: Array<WInt>): WAny {
        val prev = dict[arrSym]
        // Make sure this is array.
        if (prev != null) {
            if (prev !is WArray) {
                throw SemanticException("Cannot access index elements of non-array type: $prev")
            } else {
                // 'Peel off' one layer of array per index.
                var curr: WAny = prev
                for (idx in indices) {
                    if (curr !is WArray) {
                        // Not array, but another index is requested?
                        throw SemanticException("Type $curr is not indexable.")
                    } else {
                        curr = curr.elemType
                    }
                }
                return curr
            }
        } else {
            if (parentTable == null) {
                throw SemanticException("Attempted to reassign undeclared variable.")
            } else {
                return parentTable.get(arrSym, indices)
            }
        }
    }

    override fun getMap(): Map<String, WAny> {
        return dict
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
                dict[symbol] = value
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
                // 'Peel off' one layer of array per index.
                var curr: WAny = prev
                for (idx in indices) {
                    if (curr !is WArray) {
                        // Not array, but another index is requested?
                        throw SemanticException("Type $curr is not indexable.")
                    } else {
                        curr = curr.elemType
                    }
                }
                // Then make sure it's the same type.
                if (typesAreEqual(curr, value)) {
                    // TODO: BACK-END: Reassign value
                    return
                } else {
                    throw SemanticException("Attempted to reassign type of declared $prev to $value")
                }
            }
        } else {
            if (parentTable == null) {
                throw SemanticException("Attempted to reassign undeclared variable.")
            } else {
                parentTable.reassign(arrSym, indices, value)
            }
        }
    }

    override fun reassign(pairSym: String, fst: Boolean, value: WAny) {
        val prev = dict[pairSym]
        // Make sure this is a pair.
        if (prev != null) {
            if (prev !is WPair) {
                throw SemanticException("Cannot destructure(fst/snd) type: $prev")
            } else {
                // Extract correct element
                val elemT: WAny = if (fst) {
                    prev.leftType
                } else {
                    prev.rightType
                }
                // Make sure matching type
                if (typesAreEqual(elemT, value)) {
                    // TODO: BACK-END: Reassign value
                    return
                } else {
                    throw SemanticException("Attempted to reassign type of declared $prev to $value")
                }
            }
        } else {
            if (parentTable == null) {
                throw SemanticException("Attempted to reassign undeclared variable.")
            } else {
                parentTable.reassign(pairSym, fst, value)
            }
        }
    }

    override fun createChildScope(): SymbolTable {
        return ParentRefSymbolTable(this, false)
    }

    override fun toString(): String {
        return "${this.hashCode().toString(16)}, $dict, parent:${
            parentTable?.hashCode()?.toString(16)
        }, ${parentTable.toString()}"
    }

}
