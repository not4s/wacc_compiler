package ast

import org.antlr.v4.runtime.ParserRuleContext
import semantic.SemanticChecker
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import waccType.WAny
import waccType.WBool
import waccType.WChar
import waccType.WInt

/**
 * The AST Node for Unary Operations
 **/
class UnaryOperation(
    override val st: SymbolTable,
    private val operand: Expr,
    val op: UnOperator,
    parserCtx: ParserRuleContext,
) : Expr {

    private val errorMessageBuilder: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    override fun check() {
        SemanticChecker.checkThatOperationTypeIsValid(operand.type, errorMessageBuilder, op)
    }

    override fun toString(): String {
        return "$op\n" + "  (scope:$st)\n${operand.toString().prependIndent(INDENT)}"
    }

    override val type: WAny
        get() = when (op) {
            UnOperator.NOT -> WBool()
            UnOperator.CHR -> WChar()
            else -> WInt()
        }
}