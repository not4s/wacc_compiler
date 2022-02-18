package ast

import antlr.WACCParser
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import symbolTable.SymbolTable
import utils.PositionedError
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.*

const val INDENT = "  | "

interface AST {
    /**
     *  Information in the symbol table is the mapping from variable and
     *  function identifiers to its type The symbol table attribute references the table,
     *  corresponding to its nearest scope
     */
    val st: SymbolTable

    /**
     *  Converts the AST node into a string containing the information of that node
     */
    override fun toString(): String
}

interface Typed : AST {
    val type: WAny
}

interface RHS : AST, Typed

interface LHS : AST, Typed

interface Expr : AST, Typed, RHS

interface Stat : AST

/**
 * SemanticErrorMessageBuilder smart constructor from Parse Context and Symbol Table
 * @return the incomplete builder which is commonly created.
 * Helps avoid duplication.
 */
fun builderTemplateFromContext(
    parserCtx: ParserRuleContext,
    st: SymbolTable
): SemanticErrorMessageBuilder {
    return SemanticErrorMessageBuilder()
        .provideStart(PositionedError(parserCtx))
        .setLineTextFromSrcFile(st.srcFilePath)
}

/**
 * Types of the different binary operations
 **/
enum class BinOperator {
    MUL, DIV, MOD, ADD, SUB, GT, GEQ, LT, LEQ, EQ, NEQ, AND, OR;

    companion object {

        fun isForInt(it: BinOperator): Boolean = when (it) {
            MUL, DIV, MOD, ADD, SUB -> true
            else -> false
        }

        fun isOrdering(it: BinOperator): Boolean = when (it) {
            GT, GEQ, LT, LEQ -> true
            else -> false
        }

        fun isForAnyType(it: BinOperator): Boolean = when (it) {
            EQ, NEQ -> true
            else -> false
        }

        fun isForBool(it: BinOperator): Boolean = when (it) {
            AND, OR -> true
            else -> false
        }

        fun fromWACCParserContextBinOp(binOp: Token): BinOperator = when (binOp.type) {
            WACCParser.OP_MULT -> MUL
            WACCParser.OP_DIV -> DIV
            WACCParser.OP_MOD -> MOD
            WACCParser.OP_ADD -> ADD
            WACCParser.OP_SUBT -> SUB
            WACCParser.OP_GT -> GT
            WACCParser.OP_GEQ -> GEQ
            WACCParser.OP_LT -> LT
            WACCParser.OP_LEQ -> LEQ
            WACCParser.OP_EQ -> EQ
            WACCParser.OP_NEQ -> NEQ
            WACCParser.OP_AND -> AND
            WACCParser.OP_OR -> OR
            else -> throw Exception("Unknown binary operand")
        }
    }
}

/**
 * Types of the different unary operations
 **/
enum class UnOperator {
    NOT, ORD, CHR, LEN, SUB;

    companion object {
        fun fromWACCParserContextUnOp(unOp: Token): UnOperator = when (unOp.type) {
            WACCParser.OP_NOT -> NOT
            WACCParser.OP_ORD -> ORD
            WACCParser.OP_CHR -> CHR
            WACCParser.OP_LEN -> LEN
            WACCParser.OP_SUBT -> SUB
            else -> throw Exception("Unknown unary operand")
        }
    }
}

/**
 *  The AST Node for Base Type Literals
 **/
class Literal(
    override val st: SymbolTable,
    override val type: WBase,
) : Expr, RHS {

    override fun toString(): String {
        return "Literal\n  (scope:$st)\n${("type: $type").prependIndent(INDENT)}"
    }
}

class WACCType(
    override val st: SymbolTable,
    override val type: WAny
) : Typed {

    override fun toString(): String {
        return "WACCType: $type"
    }
}

/**
 *  The AST Node for Pair Literals
 **/
class PairLiteral(
    override val st: SymbolTable,
    override val type: WPairNull,
) : Expr {

    override fun toString(): String {
        return "PairLiteral(null)\n  (scope:$st)\n${("type: $type").prependIndent(INDENT)}"
    }
}

/**
 * The AST Node for RHS New Pair
 **/
class NewPairRHS(
    override val st: SymbolTable,
    private val left: Expr,
    private val right: Expr,
    override val type: WPair,
) : RHS {
    override fun toString(): String {
        return "NEWPAIR:\n  (scope:$st)\nleft:\n${
            left.toString().prependIndent(INDENT)
        }\nright:\n" + right.toString().prependIndent(INDENT)
    }
}