package ast

import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.WAny
import waccType.WArray
import waccType.WUnknown
import waccType.typesAreEqual

/**
 *  The AST Node for Array Literals
 **/
class ArrayLiteral(
    override val st: SymbolTable,
    private val values: Array<WAny>,
    parserCtx: ParserRuleContext
) : Expr, RHS {
    private val semanticErrorMessage: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    /**
     * Calling a getter to potentially trigger exception throwing
     * @throws SemanticException if array entries have inconsistent types
     */
    override fun check() {
        type
    }

    override val type: WArray
        get() =
            if (values.isEmpty()) {
                WArray(WUnknown())
            } else {
                val expType: WAny = values.first()
                for (elem in values) {
                    if (!typesAreEqual(elem, expType)) {
                        semanticErrorMessage.arrayEntriesTypeClash().buildAndPrint()
                        throw SemanticException("Types in array are not equal: $elem, $expType")
                    }
                }
                WArray(expType)
            }

    override fun toString(): String {
        return "ArrayLiteral\n  (scope:$st)\n${
            ("type: $type\nelements: [${
                values.map { e -> e.toString() }.reduceOrNull { a, b -> "$a $b" } ?: ""
            }]").prependIndent(INDENT)
        }"
    }
}