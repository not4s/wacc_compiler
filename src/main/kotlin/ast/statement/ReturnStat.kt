package ast.statement

import ast.*
import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.WAny

/**
 * The AST Node for Return Statements
 **/
class ReturnStat(
    override val st: SymbolTable,
    val expression: Expr,
    parserCtx: ParserRuleContext,
) : Stat, Typed {

    private val semanticErrorMessage: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    override val type: WAny
        get() = expression.type

    /**
     * Checks the scope
     */
    override fun check() {
        if (st.isGlobal) {
            semanticErrorMessage
                .returnFromGlobalScope()
                .buildAndPrint()
            throw SemanticException("Cannot return out of global scope.")
        }
    }

    override fun toString(): String {
        return "Return:\n" + "  (scope:$st)\n${expression.toString().prependIndent(INDENT)}"
    }
}