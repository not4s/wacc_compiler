package ast.statement

import ast.Expr
import ast.INDENT
import ast.Stat
import symbolTable.SymbolTable

/**
 * The AST Node for Free Statements
 **/
class FreeStat(
    override val st: SymbolTable,
    val expression: Expr,
) : Stat {
    override fun toString(): String {
        return "Free:\n" + "  (scope:$st)\n${expression.toString().prependIndent(INDENT)}"
    }
}