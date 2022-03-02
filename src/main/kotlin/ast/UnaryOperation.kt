package ast

import symbolTable.SymbolTable
import waccType.WAny
import waccType.WBool
import waccType.WChar
import waccType.WInt

/**
 * The AST Node for Unary Operations
 **/
class UnaryOperation(
    override val st: SymbolTable,
    val operand: Expr,
    val operation: UnOperator,
) : Expr {

    override fun toString(): String {
        return "$operation\n" + "  (scope:$st)\n${operand.toString().prependIndent(INDENT)}"
    }

    override val type: WAny
        get() = when (operation) {
            UnOperator.NOT -> WBool()
            UnOperator.CHR -> WChar()
            else -> WInt()
        }
}