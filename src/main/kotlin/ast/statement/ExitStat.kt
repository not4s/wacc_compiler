package ast.statement

import ast.Expr
import ast.INDENT
import ast.Stat
import symbolTable.SymbolTable

/**
 * The AST Node for Exit Statements
 **/
class ExitStat(
    override val st: SymbolTable,
    val expr: Expr,
) : Stat {
    override fun toString(): String {
        return "Exit:\n" + "  (scope:$st)\n${expr.toString().prependIndent(INDENT)}"
    }
}