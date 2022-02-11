package ast

import ast.statement.*
import org.antlr.v4.runtime.ParserRuleContext
import symbolTable.SymbolTable
import utils.ExitCode
import utils.PositionedError
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.*
import kotlin.system.exitProcess

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
 * @exception ExitCode.SYNTAX_ERROR
 **/
fun hasReturn(stat: Stat, inOuterFuncScope: Boolean): Boolean {
    return when (stat) {
        is ReturnStat -> true
        is ExitStat -> true
        is JoinStat -> if (hasReturn(stat.first, true) && !hasReturn(stat.second, false) && inOuterFuncScope) {
            println("Should not have return before another non-return statement.")
            exitProcess(ExitCode.SYNTAX_ERROR)
        } else {
            hasReturn(stat.second, true)
        }
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
 * @exception SemanticException
 **/
fun checkReturnType(stat: Stat, expected: WAny, errBuilder: SemanticErrorMessageBuilder) {
    when (stat) {
        is ReturnStat -> if (!typesAreEqual(stat.type, expected)) {
            errBuilder.functionReturnStatTypeMismatch(expected, stat.type).buildAndPrint()
            throw SemanticException("Mismatching return type for function, expected: $expected, got: ${stat.type} ")
        }
        is JoinStat -> {
            checkReturnType(stat.first, expected, errBuilder)
            checkReturnType(stat.second, expected, errBuilder)
        }
        is IfThenStat -> {
            checkReturnType(stat.thenStat, expected, errBuilder)
            checkReturnType(stat.elseStat, expected, errBuilder)
        }
        is WhileStat -> checkReturnType(stat.doBlock, expected, errBuilder)
    }
}