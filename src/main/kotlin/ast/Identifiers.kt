package ast

import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.WAny
import waccType.typesAreEqual

/**
 * The AST Node for Setting Identifiers
 **/
class IdentifierSet(
    override val st: SymbolTable,
    val identifier: String,
    parserCtx: ParserRuleContext
) : LHS {
    private val semanticErrorMessage: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)
    override fun toString(): String {
        return "IdentifierSet:\n" + "  (scope:$st)\n${("identifier: $identifier").prependIndent(INDENT)}\n${
            ("type: $type").prependIndent(INDENT)
        }"
    }

    override val type: WAny
        get() = st.get(identifier, semanticErrorMessage)
}

/**
 * The AST Node for Getting Identifiers
 **/
class IdentifierGet(
    override val st: SymbolTable,
    val identifier: String,
    parserCtx: ParserRuleContext
) : Expr {

    private val semanticErrorMessage: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    override fun check() {
        if (!typesAreEqual(st.get(identifier, semanticErrorMessage), type)) {
            semanticErrorMessage
                .operandTypeMismatch(st.get(identifier, semanticErrorMessage), type)
                .appendCustomErrorMessage(
                    "$identifier has a type which does not match with the type of the right hand side."
                )
                .buildAndPrint()
            throw SemanticException(
                "Attempted to use variable of type ${
                    st.get(
                        identifier,
                        semanticErrorMessage
                    )
                } as $type"
            )
        }
    }

    override fun toString(): String {
        return "IdentifierGet:\n" + "  (scope:$st)\n${("identifier: $identifier").prependIndent(INDENT)}\n${
            ("type: $type").prependIndent(INDENT)
        }"
    }

    override val type: WAny
        get() = st.get(identifier, semanticErrorMessage)
}