package ast

import org.antlr.v4.runtime.ParserRuleContext
import semantic.SemanticChecker
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
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

    private val errorMessageBuilder: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    /**
     * Check that operands have the same type
     * Then checks that binary operation can be applied to operands of such types
     */
    override fun check() {
        SemanticChecker.checkThatOperandTypesMatch(
            firstType = left.type,
            secondType = right.type,
            errorMessageBuilder= errorMessageBuilder,
            extraMessage = "Binary operation cannot be executed correctly",
            failMessage = "Attempted to call binary operation $op on unequal types: ${left.type}, ${right.type}"
        )
        SemanticChecker.checkThatOperationTypeIsValid(
            operandType = left.type,
            errorMessageBuilder = errorMessageBuilder,
            operation = op
        )
    }

    override fun toString(): String {
        return "$op\n" + "  (scope:$st)\n${left.toString().prependIndent(INDENT)}\n${
            right.toString().prependIndent(INDENT)
        }"
    }

    override val type: WAny
        get() = if (BinOperator.isForInt(op)) WInt() else WBool()
}