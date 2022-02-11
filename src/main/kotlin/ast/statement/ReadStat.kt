package ast.statement

import ast.INDENT
import ast.LHS
import ast.Stat
import ast.builderTemplateFromContext
import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.WChar
import waccType.WInt

/**
 * The AST Node for Read Statements
 **/
class ReadStat(
    override val st: SymbolTable,
    private val lhs: LHS,
    parserCtx: ParserRuleContext,
) : Stat {

    private val semanticErrorMessage: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    override fun check() {
        if (lhs.type !is WChar && lhs.type !is WInt) {
            semanticErrorMessage
                .readTypeIsIncorrect(lhs.type)
                .buildAndPrint()
            throw SemanticException("Cannot read into non-char or non-int variable, actual: ${lhs.type}")
        }
    }

    override fun toString(): String {
        return "Read:\n" + "  (scope:$st)\n${"LHS:\n${lhs.toString().prependIndent(INDENT)}"}"
    }
}