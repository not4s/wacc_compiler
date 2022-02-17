package ast.statement

import ast.Expr
import ast.INDENT
import ast.Stat
import ast.builderTemplateFromContext
import org.antlr.v4.runtime.ParserRuleContext
import semantic.SemanticChecker
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder

/**
 * The AST Node for Exit Statements
 **/
class ExitStat(
    override val st: SymbolTable,
    private val expression: Expr,
    parserCtx: ParserRuleContext,
) : Stat {

    private val errorMessageBuilder: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    override fun check() {
        SemanticChecker.checkExprTypeIsWInt(
            type = expression.type,
            errorMessageBuilder = errorMessageBuilder,
            failMessage = "Cannot exit with non-int expression. Actual: ${expression.type}"
        )
    }

    override fun toString(): String {
        return "Exit:\n" + "  (scope:$st)\n${expression.toString().prependIndent(INDENT)}"
    }
}