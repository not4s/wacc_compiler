package ast.statement

import ast.Expr
import ast.INDENT
import ast.Stat
import symbolTable.SymbolTable

/**
 * The AST Node for While Statements
 **/
class WhileStat(
    override val st: SymbolTable,
    val condition: Expr,
    val doBlock: Stat,
) : Stat {
    override fun toString(): String {
        return "While-do:\n" + "  (scope:$st)\n${
            ("condition:\n${
                condition.toString().prependIndent("   ")
            }").prependIndent(INDENT)
        }\n${
            ("do:\n${doBlock.toString().prependIndent("   ")}").prependIndent(INDENT)
        }"
    }
}