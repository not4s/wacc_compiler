package ast

import ast.statement.SkipStat
import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.WAny
import waccType.WUnknown
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

    private val semanticErrorMessage: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    override fun check() {
        // Check params against st
        val func = st.get(identifier, semanticErrorMessage) as WACCFunction

        // If function is not yet defined, just return
        if (func.body is SkipStat && func.params.isEmpty() && func.type is WUnknown) {
            return
        }
        if (func.params.size != params.size) {
            semanticErrorMessage
                .functionArgumentCountMismatch(func.params.size, params.size)
                .buildAndPrint()
            throw SemanticException("Argument count does not match up with expected count for function $identifier")
        }
        func.params.onEachIndexed { i, (_, v) ->
            if (!typesAreEqual(v, params[i].type)) {
                val actualType = params[i].type
                semanticErrorMessage
                    .functionArgumentTypeMismatch(v, actualType)
                    .buildAndPrint()
                throw SemanticException("Mismatching types for function $identifier call: expected $v, got $actualType")
            }
        }
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
        get() = (st.get(identifier, semanticErrorMessage) as WACCFunction).type
}