package ast

import org.antlr.v4.runtime.ParserRuleContext
import semantic.SemanticChecker
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.WAny
import waccType.WInt

/**
 * The AST Node for Array Elements
 * @property identifier : is the variable name of the array
 * @property indices : List of child ASTs
 **/
class ArrayElement(
    override val st: SymbolTable,
    val identifier: String,
    val indices: Array<Expr>,
    parserCtx: ParserRuleContext,
) : LHS, Expr {

    private val errorMessageBuilder: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    /**
     *  Here the semantic analysis is conducted inside getter
     */
    override fun check() {
        this.type
    }

    override fun toString(): String {
        return "ArrayElem:\n" + "  (scope:$st)\n${
            ("array identifier: $identifier\nindex/ices:\n${
                (indices.map { e -> e.toString() }
                    .reduceOrNull { a, b -> "$a\n$b" } ?: "").prependIndent("  ")
            }").prependIndent(INDENT)
        }\n${
            ("type: $type").prependIndent(INDENT)
        }"
    }

    override val type: WAny
        get() = st.get(
            arrSym = identifier,
            indices = indices.map { expr ->
                SemanticChecker.takeExprTypeAsWIntWithCheck(expr, errorMessageBuilder)
            }.toTypedArray(),
            errorMessageBuilder = errorMessageBuilder
        )
}