package ast.statement

import ast.*
import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import syntax.SyntaxChecker
import utils.ExitCode
import utils.SemanticErrorMessageBuilder
import waccType.WAny
import kotlin.system.exitProcess

/**
 * The AST Node for Functions
 * @property type : return type of the function
 **/
class WACCFunction(
    override val st: SymbolTable,
    val identifier: String,
    val params: Map<String, WAny>,
    val body: Stat,
    override val type: WAny,
    parserCtx: ParserRuleContext
) : AST, Typed, WAny {
    private val semanticErrorMessage: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)
    override fun check() {
        body.check()
        SyntaxChecker.checkFuncitonHavingReturn(body, identifier)
        checkReturnType(body, type, semanticErrorMessage)
    }

    override fun toString(): String {
        return "Function($type) $identifier(${
            params.map { (id, t) -> "($t)$id" }.reduceOrNull { a, b -> "$a, $b" } ?: ""
        })"
    }
}