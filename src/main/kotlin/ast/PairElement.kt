package ast

import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.*

/**
 * The AST Node for Pair Elements
 * @property first: is a boolean flag which determines which pair element
 * operator is used. 'true' stands for 'fst' and 'false' for 'snd'
 **/
class PairElement(
    override val st: SymbolTable,
    val first: Boolean, // true = fst, false = snd
    val expr: Expr,
    parserCtx: ParserRuleContext,
) : LHS, RHS {

    private var actualType: WAny? = null

    private val semanticErrorMessage: SemanticErrorMessageBuilder = builderTemplateFromContext(parserCtx, st)

    init {
        check()
    }

    override fun check() {
        if (expr.type is WPairKW) {
            return
        }
        if (expr.type !is WPair) {
            semanticErrorMessage
                .pairElementInvalidType()
                .buildAndPrint()
            throw SemanticException("Cannot call 'fst' or 'snd' on non-pair type: ${expr.type}")
        }
        // Check null
        if (expr is PairLiteral) {
            throwNullDereferenceSemanticException()
        }
    }

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
            if (expr.type is WPairNull) {
                throwNullDereferenceSemanticException()
            }
            if (expr.type is WPairKW) {
                return WUnknown()
            }
            val pair = expr.type as WPair
            return if (first) pair.leftType else pair.rightType
        }

    private fun throwNullDereferenceSemanticException() {
        semanticErrorMessage
            .pairElementInvalidType()
            .buildAndPrint()
        throw SemanticException("NULL POINTER EXCEPTION! Can't dereference null.")
    }

    fun updateType(type: WAny) {
        actualType = type
    }
}