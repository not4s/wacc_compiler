package ast.statement

import ast.INDENT
import ast.LHS
import ast.Stat
import ast.builderTemplateFromContext
import org.antlr.v4.runtime.ParserRuleContext
import semantic.SemanticChecker
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder

/**
 * The AST Node for Read Statements
 **/
class ReadStat(
    override val st: SymbolTable,
    private val lhs: LHS,
    parserCtx: ParserRuleContext,
) : Stat {

    private val errorMessageBuilder: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    override fun check() {
        SemanticChecker.checkReadType(
            type = lhs.type,
            errorMessageBuilder = errorMessageBuilder,
            failMessage = "Cannot read into non-char or non-int variable, actual: ${lhs.type}"
        )
    }

    override fun toString(): String {
        return "Read:\n" + "  (scope:$st)\n${"LHS:\n${lhs.toString().prependIndent(INDENT)}"}"
    }
}