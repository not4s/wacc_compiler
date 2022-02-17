package ast

import org.antlr.v4.runtime.ParserRuleContext
import semantic.SemanticChecker
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import waccType.WAny

/**
 * The AST Node for Setting Identifiers
 **/
class IdentifierSet(
    override val st: SymbolTable,
    val identifier: String,
    parserCtx: ParserRuleContext
) : LHS {

    private val errorMessageBuilder: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    override fun toString(): String {
        return "IdentifierSet:\n" + "  (scope:$st)\n${("identifier: $identifier").prependIndent(INDENT)}\n${
            ("type: $type").prependIndent(INDENT)
        }"
    }

    override val type: WAny
        get() = st.get(identifier, errorMessageBuilder)
}

/**
 * The AST Node for Getting Identifiers
 **/
class IdentifierGet(
    override val st: SymbolTable,
    val identifier: String,
    parserCtx: ParserRuleContext
) : Expr {

    private val errorMessageBuilder: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    override fun check() {
        SemanticChecker.checkIdentifierExpressionType(type, st, identifier, errorMessageBuilder)
    }

    override fun toString(): String {
        return "IdentifierGet:\n" + "  (scope:$st)\n${("identifier: $identifier").prependIndent(INDENT)}\n${
            ("type: $type").prependIndent(INDENT)
        }"
    }

    override val type: WAny
        get() = st.get(identifier, errorMessageBuilder)
}