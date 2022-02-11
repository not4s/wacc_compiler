package ast

import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.*

/**
 * The AST Node for Unary Operations
 **/
class UnaryOperation(
    override val st: SymbolTable,
    private val operand: Expr,
    val op: UnOperator,
    parserCtx: ParserRuleContext,
) : Expr {

    private val semanticErrorMessage: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    override fun check() {
        val typeIsIncorrect: Boolean = when (op) {
            UnOperator.NOT -> operand.type !is WBool
            UnOperator.ORD -> operand.type !is WChar
            UnOperator.LEN -> operand.type !is WArray
            UnOperator.CHR, UnOperator.SUB -> operand.type !is WInt
        }
        if (typeIsIncorrect) {
            semanticErrorMessage
                .unOpInvalidType(operand.type, op.toString())
                .buildAndPrint()
            throw SemanticException("Attempted to call $op operation on invalid type: ${operand.type}")
        }
    }

    override fun toString(): String {
        return "$op\n" + "  (scope:$st)\n${operand.toString().prependIndent(INDENT)}"
    }

    override val type: WAny
        get() = when (op) {
            UnOperator.NOT -> WBool()
            UnOperator.ORD -> WInt()
            UnOperator.CHR -> WChar()
            UnOperator.LEN -> WInt()
            UnOperator.SUB -> WInt()
        }
}