package ast

import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.*

/**
 *  The AST Node for Binary Operations
 **/
class BinaryOperation(
    override val st: SymbolTable,
    private val left: Expr,
    private val right: Expr,
    val op: BinOperator,
    parserCtx: ParserRuleContext,
) : Expr {

    private val semanticErrorMessage: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    /**
     * Check that operands have the same type
     * Then checks that binary operation can be applied to operands of such types
     * @throws SemanticException whenever any of those checks fails
     */
    override fun check() {
        if (!typesAreEqual(left.type, right.type)) {
            semanticErrorMessage
                .operandTypeMismatch(left.type, right.type)
                .appendCustomErrorMessage("Binary operation cannot be executed correctly")
                .buildAndPrint()
            throw SemanticException("Attempted to call binary operation $op on unequal types: ${left.type}, ${right.type}")
        }
        val operationTypeNotValid: Boolean = when (op) {
            BinOperator.MUL, BinOperator.DIV, BinOperator.MOD, BinOperator.ADD, BinOperator.SUB -> !typesAreEqual(left.type, WInt())
            BinOperator.GT, BinOperator.GEQ, BinOperator.LT, BinOperator.LEQ -> !typesAreEqual(left.type, WInt()) && !typesAreEqual(left.type, WChar())
            BinOperator.EQ, BinOperator.NEQ -> false
            BinOperator.AND, BinOperator.OR -> !typesAreEqual(left.type, WBool())
        }
        if (operationTypeNotValid) {
            semanticErrorMessage
                .binOpInvalidType(left.type, op.toString())
                .buildAndPrint()
            throw SemanticException("Attempted to call binary operation $op on operands of invalid type: ${left.type} ")
        }
    }

    override fun toString(): String {
        return "$op\n" + "  (scope:$st)\n${left.toString().prependIndent(INDENT)}\n${
            right.toString().prependIndent(INDENT)
        }"
    }

    override val type: WAny
        get() = when (op) {
            BinOperator.MUL, BinOperator.DIV, BinOperator.MOD, BinOperator.ADD, BinOperator.SUB -> WInt()
            BinOperator.GT, BinOperator.GEQ, BinOperator.LT, BinOperator.LEQ -> WBool()
            BinOperator.EQ, BinOperator.NEQ -> WBool()
            BinOperator.AND, BinOperator.OR -> WBool()
        }
}