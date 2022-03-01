package ast

import symbolTable.SymbolTable
import waccType.WAny
import waccType.WBool
import waccType.WInt

/**
 *  The AST Node for Binary Operations
 **/
class BinaryOperation(
    override val st: SymbolTable,
    private val left: Expr,
    private val right: Expr,
    val op: BinOperator,
) : Expr {

    override fun toString(): String {
        return "$op\n" + "  (scope:$st)\n${left.toString().prependIndent(INDENT)}\n${
            right.toString().prependIndent(INDENT)
        }"
    }

    override val type: WAny
        get() = if (BinOperator.isForInt(op)) WInt() else WBool()
}