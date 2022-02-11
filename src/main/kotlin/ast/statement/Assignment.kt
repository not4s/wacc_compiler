package ast.statement

import ast.*
import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.WInt
import waccType.typesAreEqual

/**
 * The AST Node for Assignments
 **/
class Assignment(
    override val st: SymbolTable,
    private val lhs: LHS,
    private val rhs: RHS,
    parserCtx: ParserRuleContext
) : Stat {

    private val semanticErrorMessage: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    override fun check() {
        if (!typesAreEqual(lhs.type, rhs.type)) {
            semanticErrorMessage
                .operandTypeMismatch(lhs.type, rhs.type)
                .buildAndPrint()
            throw SemanticException("Cannot assign ${rhs.type} to ${lhs.type}")
        }
        when (lhs) {
            is IdentifierSet -> st.reassign(lhs.identifier, rhs.type, semanticErrorMessage)
            is ArrayElement -> {
                val indices: Array<WInt> = lhs.indices.map {
                    it.type as? WInt
                        ?: run {
                            semanticErrorMessage
                                .arrayIndexInvalidType()
                                .buildAndPrint()
                            throw SemanticException("Non-int index in array ${it.type}")
                        }
                }.toTypedArray()
                st.reassign(lhs.identifier, indices, rhs.type, semanticErrorMessage)
            }
            is PairElement -> {
                // Make sure this is: fst <identifier> = blah. Otherwise invalid.
                if (lhs.expr !is IdentifierGet) {
                    semanticErrorMessage
                        .pairElementInvalidType()
                        .buildAndPrint()
                    throw SemanticException("Cannot refer to ${lhs.type} with fst/snd")
                }
                st.reassign(lhs.expr.identifier, lhs.first, rhs.type, semanticErrorMessage)
            }
        }
    }

    override fun toString(): String {
        return "Assignment:\n" + "  (scope:$st)\n${lhs.toString().prependIndent(INDENT)}\n${
            rhs.toString().prependIndent(INDENT)
        }"
    }
}