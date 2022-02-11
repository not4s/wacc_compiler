package ast.statement

import ast.INDENT
import ast.RHS
import ast.Stat
import ast.builderTemplateFromContext
import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.WAny
import waccType.typesAreEqual

/**
 * The AST Node for Declarations
 **/
class Declaration(
    override val st: SymbolTable,
    private var decType: WAny,
    val identifier: String,
    private val rhs: RHS,
    parserCtx: ParserRuleContext,
) : Stat {

    private val semanticErrorMessage: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
        st.declare(identifier, decType, semanticErrorMessage)
    }

    override fun check() {
        if (!typesAreEqual(decType, rhs.type)) {
            semanticErrorMessage
                .operandTypeMismatch(decType, rhs.type)
                .appendCustomErrorMessage(
                    "The type of variable $identifier and the evaluated expression do not match"
                )
                .buildAndPrint()
            throw SemanticException("Attempted to declare variable $identifier of type $decType to ${rhs.type}")
        }
    }

    override fun toString(): String {
        return "Declaration:\n" +
                "  (scope:$st)\n${("of: $identifier").prependIndent(INDENT)}\n${
                    ("to: $rhs").toString().prependIndent(INDENT)
                }"
    }
}