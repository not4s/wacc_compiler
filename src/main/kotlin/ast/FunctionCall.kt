package ast

import org.antlr.v4.runtime.ParserRuleContext
import semantic.SemanticChecker
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.WAny
import waccType.typesAreEqual

/**
 *  The AST Node for Function Calls
 **/
class FunctionCall(
    override val st: SymbolTable,
    val identifier: String,
    private val params: Array<Expr>,
    parserCtx: ParserRuleContext,
) : RHS {

    private val errorMessageBuilder: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    /**
     * Checks that the argument count is correct.
     * Then checks each argument type.
     */
    override fun check() {
    }

    override fun toString(): String {
        return "Calling $identifier with parameters...:\n${
            params.mapIndexed { i, e ->
                "Parameter $i:\n${
                    e.toString().prependIndent(INDENT)
                }".prependIndent("  ")
            }.reduceOrNull { a, b -> "$a\n$b" } ?: ""
        }"
    }

    override val type: WAny
        get() = (st.get(identifier, errorMessageBuilder) as WACCFunction).type
}