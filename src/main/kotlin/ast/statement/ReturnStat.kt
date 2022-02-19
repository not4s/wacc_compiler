package ast.statement

import ast.*
import symbolTable.SymbolTable
import waccType.WAny

/**
 * The AST Node for Return Statements
 **/
class ReturnStat(
    override val st: SymbolTable,
    val expression: Expr,
) : Stat, Typed {

    override val type: WAny
        get() = expression.type

    override fun toString(): String {
        return "Return:\n" + "  (scope:$st)\n${expression.toString().prependIndent(INDENT)}"
    }
}