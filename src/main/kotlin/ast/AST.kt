package ast

import ast.statement.*
import org.antlr.v4.runtime.ParserRuleContext
import semantic.SemanticChecker
import symbolTable.SymbolTable
import syntax.SyntaxChecker
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
     *  Performs semantic analysis on the AST node and throws exceptions if semantic errors are found
     *  The default implementation does nothing, which means that it always succeeds
     *  @throws SemanticException if something goes wrong
     */
    fun check() {}

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
    }
}

/**
 * Types of the different unary operations
 **/
enum class UnOperator {
    NOT, ORD, CHR, LEN, SUB;
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
    override fun check() {
    }

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

/**
 * Checks whether the given statement has a proper return statement by matching patterns recursively
 * @param stat : statement to be checked
 * @param inOuterFuncScope : examines the scope to check context
 **/
fun hasReturn(stat: Stat, inOuterFuncScope: Boolean): Boolean {
    return when (stat) {
        is ReturnStat -> true
        is ExitStat -> true
        is JoinStat -> SyntaxChecker.unreachableCodeCheck(stat, inOuterFuncScope)
        is IfThenStat -> hasReturn(stat.thenStat, false) && hasReturn(stat.elseStat, false)
        is WhileStat -> hasReturn(stat.doBlock, false)
        else -> false
    }
}

/**
 * Checks the return type of statement by matching patterns recursively and
 * throws a semantic exception if the type does not match the expected
 * @param stat : return type to be checked
 * @param expected : expected type to be matched
 * @param errorMessageBuilder : incomplete semantic error message builder which is built in error case
 **/
fun checkReturnType(stat: Stat, expected: WAny, errorMessageBuilder: SemanticErrorMessageBuilder) {
    when (stat) {
        is ReturnStat -> SemanticChecker.checkThatReturnTypeMatch(
            firstType = expected,
            secondType = stat.type,
            errorMessageBuilder = errorMessageBuilder,
            failMessage = "Mismatching return type for function, expected: $expected, got: ${stat.type}"
        )
        is JoinStat -> {
            checkReturnType(stat.first, expected, errorMessageBuilder)
            checkReturnType(stat.second, expected, errorMessageBuilder)
        }
        is IfThenStat -> {
            checkReturnType(stat.thenStat, expected, errorMessageBuilder)
            checkReturnType(stat.elseStat, expected, errorMessageBuilder)
        }
        is WhileStat -> checkReturnType(stat.doBlock, expected, errorMessageBuilder)
    }
}