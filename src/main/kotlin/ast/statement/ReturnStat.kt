package ast.statement

import ast.*
import org.antlr.v4.runtime.ParserRuleContext
import semantic.SemanticChecker
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import waccType.WAny

/**
 * The AST Node for Return Statements
 **/
class ReturnStat(
    override val st: SymbolTable,
    val expression: Expr,
    parserCtx: ParserRuleContext,
) : Stat, Typed {

    private val errorMessageBuilder: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    override val type: WAny
        get() = expression.type

    /**
     * Checks the scope
     */
    override fun check() {
        SemanticChecker.checkGlobalScope(st, errorMessageBuilder)
    }

    override fun toString(): String {
        return "Return:\n" + "  (scope:$st)\n${expression.toString().prependIndent(INDENT)}"
    }
}