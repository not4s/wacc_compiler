package ast.statement

import ast.*
import symbolTable.ParentRefSymbolTable
import symbolTable.SymbolTable
import waccType.WAny
import waccType.WInt

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

    companion object {
        fun zero(): ReturnStat {
            val dummy = ParentRefSymbolTable("dummy")
            return ReturnStat(dummy, Literal(dummy, WInt(0)))
        }
    }
}