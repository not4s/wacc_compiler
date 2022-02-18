package ast

import org.antlr.v4.runtime.ParserRuleContext
import semantic.SemanticChecker
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
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
            indices = indices.map { it.type as? WInt ?: throw Exception("Non-WInt indices") }.toTypedArray(),
            errorMessageBuilder = errorMessageBuilder
        )
}