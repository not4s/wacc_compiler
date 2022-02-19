package ast

import org.antlr.v4.runtime.ParserRuleContext
import semantic.SemanticChecker
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import waccType.WAny
import waccType.WPair
import waccType.WPairKW
import waccType.WUnknown

/**
 * The AST Node for Pair Elements
 * @property first: is a boolean flag which determines which pair element
 * operator is used. 'true' stands for 'fst' and 'false' for 'snd'
 **/
class PairElement(
    override val st: SymbolTable,
    val first: Boolean,
    val expr: Expr,
    parserCtx: ParserRuleContext,
) : LHS, RHS {

    private var actualType: WAny? = null

    private val errorMessageBuilder: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    override fun toString(): String {
        return "Pair element:\n" + "  (scope:$st)\n${
            ("${
                if (first) "FST" else "SND"
            }:\n${expr.toString().prependIndent(INDENT)}").prependIndent(INDENT)
        }"
    }

    override val type: WAny
        get() {
            val safeActualType = actualType
            safeActualType?.run {
                return safeActualType
            }
            SemanticChecker.checkNullDereference(expr, errorMessageBuilder)
            if (expr.type is WPairKW) {
                return WUnknown()
            }
            val pair = expr.type as WPair
            return if (first) pair.leftType else pair.rightType
        }

    fun updateType(type: WAny) {
        actualType = type
    }
}