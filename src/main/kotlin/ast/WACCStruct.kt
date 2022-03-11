package ast

import symbolTable.SymbolTable
import waccType.WAny
import waccType.WStruct

class WACCStruct(
    override val st: SymbolTable,
    override val identifier: String,
    val params: Map<String, WAny>
) : AST, WStruct(identifier) {
    override fun toString(): String {
        return "struct $identifier{${
            params.map { (id, t) -> "$t $id" }.reduceOrNull { a, b -> "$a, $b" } ?: throw Exception(
                "Cannot have no elements in a struct, this should have been handled during syntax analysis"
            )
        }}"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is WACCStruct && other !is WStruct) {
            return false
        }
        return (other as WStruct).identifier == this.identifier
    }
}