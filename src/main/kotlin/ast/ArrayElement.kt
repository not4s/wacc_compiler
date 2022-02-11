package ast

import org.antlr.v4.runtime.ParserRuleContext
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

    private val semanticErrorMessage: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

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
        get() = st.get(identifier, indices.map { e ->
            e.type as? WInt
                ?: run {
                    semanticErrorMessage
                        .arrayIndexInvalidType()
                        .buildAndPrint()
                    throw SemanticException("Cannot use non-int index for array, actual: ${e.type}")
                }
        }.toTypedArray(), semanticErrorMessage)
}