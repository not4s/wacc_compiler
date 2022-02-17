package ast.statement

import ast.*
import org.antlr.v4.runtime.ParserRuleContext
import semantic.SemanticChecker
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import waccType.WInt

/**
 * The AST Node for Assignments
 **/
class Assignment(
    override val st: SymbolTable,
    private val lhs: LHS,
    private val rhs: RHS,
    parserCtx: ParserRuleContext
) : Stat {

    private val errorMessageBuilder: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    override fun check() {
        SemanticChecker.checkThatOperandTypesMatch(lhs.type, rhs.type, errorMessageBuilder,
            failMessage = "Cannot assign ${rhs.type} to ${lhs.type}"
        )
        when (lhs) {
            is IdentifierSet -> st.reassign(lhs.identifier, rhs.type, errorMessageBuilder)
            is ArrayElement -> {
                val indices: Array<WInt> = lhs.indices.map {
                    SemanticChecker.takeExprTypeAsWIntWithCheck(it, errorMessageBuilder)
                }.toTypedArray()
                st.reassign(lhs.identifier, indices, rhs.type, errorMessageBuilder)
            }
            is PairElement -> {
                val msg = "Cannot refer to ${lhs.type} with fst/snd"
                SemanticChecker.checkThatLhsPairExpressionIsIdentifier(lhs.expr, errorMessageBuilder, msg, msg)
                val identifier = (lhs.expr as IdentifierGet).identifier
                st.reassign(identifier, lhs.first, rhs.type, errorMessageBuilder)
            }
        }
    }

    override fun toString(): String {
        return "Assignment:\n" + "  (scope:$st)\n${lhs.toString().prependIndent(INDENT)}\n${
            rhs.toString().prependIndent(INDENT)
        }"
    }
}