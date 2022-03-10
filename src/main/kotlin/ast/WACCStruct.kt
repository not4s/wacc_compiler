package ast

import symbolTable.SymbolTable
import waccType.WAny

class WACCStruct(
    override val st: SymbolTable,
    val identifier: String,
    val params: Map<String, WAny>
) : AST {
    override fun toString(): String {
        return "Struct $identifier{${
            params.map { (id, t) -> "($t)$id" }.reduceOrNull { a, b -> "$a, $b" } ?: ""
        }}"
    }
}