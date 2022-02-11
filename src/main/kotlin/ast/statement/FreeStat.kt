package ast.statement

import ast.Expr
import ast.INDENT
import ast.Stat
import ast.builderTemplateFromContext
import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.WPair

/**
 * The AST Node for Free Statements
 **/
class FreeStat(
    override val st: SymbolTable,
    val expr: Expr,
    parserCtx: ParserRuleContext,
) : Stat {

    private val semanticErrorMessage: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    /**
     * Ensures that the type of the expression to be freed is a pair
     */
    override fun check() {
        if (expr.type !is WPair) {
            semanticErrorMessage
                .freeNonPair()
                .buildAndPrint()
            throw SemanticException("This isn't C, you can't free a $expr")
        }
    }

    override fun toString(): String {
        return "Free:\n" + "  (scope:$st)\n${expr.toString().prependIndent(INDENT)}"
    }
}