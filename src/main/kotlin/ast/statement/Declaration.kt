package ast.statement

import ast.INDENT
import ast.RHS
import ast.Stat
import ast.builderTemplateFromContext
import org.antlr.v4.runtime.ParserRuleContext
import semantic.SemanticChecker
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import waccType.WAny

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

    private val errorMessageBuilder: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
        st.declare(identifier, decType, errorMessageBuilder)
    }

    override fun check() {
        SemanticChecker.checkThatOperandTypesMatch(
            firstType = decType,
            secondType = rhs.type,
            errorMessageBuilder = errorMessageBuilder,
            extraMessage = "The type of variable $identifier and the evaluated expression do not match",
            failMessage = "Attempted to declare variable $identifier of type $decType to ${rhs.type}"
        )
    }

    override fun toString(): String {
        return "Declaration:\n" +
                "  (scope:$st)\n${("of: $identifier").prependIndent(INDENT)}\n${
                    ("to: $rhs").toString().prependIndent(INDENT)
                }"
    }
}