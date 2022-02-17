package ast

import org.antlr.v4.runtime.ParserRuleContext
import semantic.SemanticChecker
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.WAny
import waccType.WArray
import waccType.WUnknown

/**
 * The AST Node for Array Literals
 **/
class ArrayLiteral(
    override val st: SymbolTable,
    private val values: Array<Expr>,
    parserCtx: ParserRuleContext
) : Expr, RHS {

    private val errorMessageBuilder: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    /**
     * Calling a getter to potentially trigger exception throwing
     * @throws SemanticException if array entries have inconsistent types
     */
    override fun check() {
        this.type
    }

    override val type: WArray
        get() {
            if (values.isEmpty()) {
                return WArray(WUnknown())
            }
            val expType: WAny = values.first().type
            values.forEach {
                SemanticChecker.checkThatArrayElementsTypeMatch(it.type, expType, errorMessageBuilder)
            }
            return WArray(expType)
        }

    override fun toString(): String {
        return "ArrayLiteral\n  (scope:$st)\n${
            ("type: $type\nelements: [${
                values.map { e -> e.toString() }.reduceOrNull { a, b -> "$a $b" } ?: ""
            }]").prependIndent(INDENT)
        }"
    }
}