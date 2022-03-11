package ast

import symbolTable.SymbolTable
import waccType.WAny

class WACCStruct(
    override val st: SymbolTable,
    val identifier: String,
    val params: Map<String, WAny>
) : AST, WAny {
    override fun toString(): String {
        return "Struct $identifier{${
            params.map { (id, t) -> "$t $id" }.reduceOrNull { a, b -> "$a, $b" } ?: throw Exception(
                "Cannot have no elements in a struct, this should have been handled during syntax analysis"
            )
        }}"
    }
}