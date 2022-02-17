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
import waccType.WBool

/**
 * The AST Node for If then Statements
 **/
class IfThenStat(
    override val st: SymbolTable,
    val condition: Expr,
    val thenStat: Stat,
    val elseStat: Stat,
    parserCtx: ParserRuleContext,
) : Stat {

    private val errorMessageBuilder: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    override fun check() {
        SemanticChecker.checkExprTypeIsWBool(
            type = condition.type,
            errorMessageBuilder = errorMessageBuilder,
            failMessage = "If statement has non-bool condition, actual: ${condition.type}"
        )
        thenStat.check()
        elseStat.check()
    }

    override fun toString(): String {
        return "If-Then-Else:\n" + "  (scope:$st)\n${
            ("if:\n${
                condition.toString().prependIndent("   ")
            }").prependIndent(INDENT)
        }\n${
            ("then:\n${thenStat.toString().prependIndent("   ")}").prependIndent(INDENT)
        }\n${("else:\n${elseStat.toString().prependIndent("   ")}").prependIndent(INDENT)}"
    }
}