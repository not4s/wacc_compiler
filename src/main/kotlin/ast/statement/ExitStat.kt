package ast.statement

import ast.Expr
import ast.INDENT
import ast.Stat
import ast.builderTemplateFromContext
import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.WInt

/**
 * The AST Node for Exit Statements
 **/
class ExitStat(
    override val st: SymbolTable,
    private val expression: Expr,
    parserCtx: ParserRuleContext,
) : Stat {

    private val semanticErrorMessage: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    override fun check() {
        if (expression.type !is WInt) {
            semanticErrorMessage
                .nonIntExpressionExit(expression.type)
                .buildAndPrint()
            throw SemanticException("Cannot exit with non-int expression. Actual: ${expression.type}")
        }
    }

    override fun toString(): String {
        return "Exit:\n" + "  (scope:$st)\n${expression.toString().prependIndent(INDENT)}"
    }
}