package ast.statement

import ast.Expr
import ast.INDENT
import ast.Stat
import ast.builderTemplateFromContext
import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.WBool

/**
 * The AST Node for While Statements
 **/
class WhileStat(
    override val st: SymbolTable,
    val condition: Expr,
    val doBlock: Stat,
    parserCtx: ParserRuleContext,
) : Stat {

    private val semanticErrorMessage: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    override fun check() {
        if (condition.type !is WBool) {
            semanticErrorMessage
                .whileStatConditionHasNonBooleanType(condition.type)
                .buildAndPrint()
            throw SemanticException("While loop has non-bool condition, actual: ${condition.type}")
        }
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