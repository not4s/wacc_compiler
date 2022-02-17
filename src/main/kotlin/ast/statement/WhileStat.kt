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
 * The AST Node for While Statements
 **/
class WhileStat(
    override val st: SymbolTable,
    val condition: Expr,
    val doBlock: Stat,
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
            failMessage = "While loop has non-bool condition, actual: ${condition.type}"
        )
        doBlock.check()
    }

    override fun toString(): String {
        return "While-do:\n" + "  (scope:$st)\n${
            ("condition:\n${
                condition.toString().prependIndent("   ")
            }").prependIndent(INDENT)
        }\n${
            ("do:\n${doBlock.toString().prependIndent("   ")}").prependIndent(INDENT)
        }"
    }
}