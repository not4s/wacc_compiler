package ast

import symbolTable.SymbolTable
import waccType.WAny

/**
 * The AST Node for Functions
 * @property type : return type of the function
 **/
class WACCFunction(
    override val st: SymbolTable,
    val identifier: String,
    val params: Map<String, WAny>,
    val body: Stat,
    override val type: WAny
) : AST, Typed, WAny {
    override fun toString(): String {
        return "Function($type) $identifier(${
            params.map { (id, t) -> "($t)$id" }.reduceOrNull { a, b -> "$a, $b" } ?: ""
        })"
    }
}