package ast.statement

import ast.INDENT
import ast.RHS
import ast.Stat
import ast.builderTemplateFromContext
import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import waccType.WAny

/**
 * The AST Node for Declarations
 **/
class Declaration(
    override val st: SymbolTable,
    decType: WAny,
    val identifier: String,
    val rhs: RHS,
    parserCtx: ParserRuleContext,
) : Stat {

    private val errorMessageBuilder: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        st.declare(identifier, decType, errorMessageBuilder)
    }

    override fun toString(): String {
        return "Declaration:\n" +
                "  (scope:$st)\n${("of: $identifier").prependIndent(INDENT)}\n${
                    ("to: $rhs").toString().prependIndent(INDENT)
                }"
    }
}