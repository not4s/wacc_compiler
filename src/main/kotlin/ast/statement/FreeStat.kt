package ast.statement

import ast.Expr
import ast.INDENT
import ast.Stat
import ast.builderTemplateFromContext
import org.antlr.v4.runtime.ParserRuleContext
import semantic.SemanticChecker
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.WPair

/**
 * The AST Node for Free Statements
 **/
class FreeStat(
    override val st: SymbolTable,
    val expression: Expr,
    parserCtx: ParserRuleContext,
) : Stat {

    private val errorMessageBuilder: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    /**
     * Ensures that the type of the expression to be freed is a pair
     */
    override fun check() {
        SemanticChecker.checkExprTypeIsWPair(
            type = expression.type,
            errorMessageBuilder = errorMessageBuilder,
            failMessage = "This isn't C, you can't free a $expression"
        )
    }

    override fun toString(): String {
        return "Free:\n" + "  (scope:$st)\n${expression.toString().prependIndent(INDENT)}"
    }
}