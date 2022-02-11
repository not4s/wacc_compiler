package ast

import ast.BinOperator.*
import ast.UnOperator.*
import org.antlr.v4.runtime.ParserRuleContext
import org.intellij.lang.annotations.Identifier
import symbolTable.SymbolTable
import utils.ExitCode
import utils.PositionedError
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.*
import kotlin.math.exp
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
 * The AST Node for Functions
 * @property type : return type of the function
 **/
class WACCFunction(
    override val st: SymbolTable,
    val identifier: String,
    val params: Map<String, WAny>,
    val body: Stat,
    override val type: WAny,
) : AST, Typed, WAny {

    override fun check() {
        // TODO: Param checking?
        body.check()
        if (!hasReturn(body, true)) {
            println("Function $identifier does not return on every branch.")
            exitProcess(ExitCode.SYNTAX_ERROR)
        }
        checkReturnType(body, type)
    }

    override fun toString(): String {
        return "Function($type) $identifier(${
            params.map { (id, t) -> "($t)$id" }.reduceOrNull { a, b -> "$a, $b" } ?: ""
        }):\n${"   ".prependIndent(INDENT)}"
    }
}

/**
 *  The AST Node for Function Calls
 **/
class FunctionCall(
    override val st: SymbolTable,
    val identifier: String,
    private val params: Array<Expr>,
    parserCtx: ParserRuleContext,
) : RHS {

    val semanticErrorMessage: SemanticErrorMessageBuilder
        = SemanticErrorMessageBuilder().provideStart(PositionedError(parserCtx))

    init {
        check()
    }

    override fun check() {
        // Check params against st
        val func = st.get(identifier) as WACCFunction

        // If function is not yet defined, just return
        if (func.body is SkipStat && func.params.isEmpty() && func.type is WUnknown) {
            return
        }
        if (func.params.size != params.size) {
            semanticErrorMessage
                .functionArgumentCountMismatch()
                .buildAndPrint()
            throw SemanticException("Argument count does not match up with expected count for function $identifier")
        }
        func.params.onEachIndexed { i, (_, v) ->
            if (!typesAreEqual(v, params[i].type)) {
                val actualType = params[i].type
                semanticErrorMessage
                    .functionArgumentTypeMismatch(v, actualType)
                    .buildAndPrint()
                throw SemanticException("Mismatching types for function $identifier call: expected $v, got $actualType")
            }
        }
    }

    override fun toString(): String {
        return "Calling $identifier with parameters...:\n${
            params.mapIndexed { i, e ->
                "Parameter $i:\n${
                    e.toString().prependIndent(INDENT)
                }".prependIndent("  ")
            }.reduceOrNull { a, b -> "$a\n$b" } ?: ""
        }"
    }

    override val type: WAny
        get() = (st.get(identifier) as WACCFunction).type
}

/**
 *  The AST Node for Base Type Literals
 **/
class Literal(
    override val st: SymbolTable,
    override val type: WBase,
) : Expr, RHS {
    override fun check() {}

    override fun toString(): String {
        return "Literal\n  (scope:$st)\n${("type: $type").prependIndent(INDENT)}"
    }
}

/**
 *  The AST Node for Array Literals
 **/
class ArrayLiteral(
    override val st: SymbolTable,
    val values: Array<WAny>,
) : Expr, RHS {
    init {
        check()
    }
    override fun check() {
        type
    }

    override val type: WArray
        get() =
            if (values.isEmpty()) {
                WArray(WUnknown())
            } else {
            val expType : WAny = values.first()
                for (elem in values) {
                    if (!typesAreEqual(elem, expType)) {
                        throw SemanticException("Types in array are not equal: $elem, $expType")
                    }
                }
                WArray(expType)
            }

    override fun toString(): String {
        return "ArrayLiteral\n  (scope:$st)\n${
            ("type: $type\nelements: [${
                values.map { e -> e.toString() }.reduceOrNull { a, b -> "$a $b" } ?: ""
            }]").prependIndent(INDENT)
        }"
    }
}

class WACCType(override val st: SymbolTable, override val type: WAny) : Typed {
    override fun check() {

    }

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
    val left: Expr,
    val right: Expr,
    override val type: WPair,
) : RHS {

    override fun toString(): String {
        return "NEWPAIR:\n  (scope:$st)\nleft:\n${
            left.toString().prependIndent(INDENT)
        }\nright:\n" + right.toString().prependIndent(INDENT)
    }
}

/**
 *  The AST Node for Binary Operations
 **/
class BinaryOperation(
    override val st: SymbolTable,
    val left: Expr,
    val right: Expr,
    val op: BinOperator,
    parserCtx: ParserRuleContext,
) : Expr {

    private val semanticErrorMessage: SemanticErrorMessageBuilder
        = SemanticErrorMessageBuilder().provideStart(PositionedError(parserCtx))

    init {
        check()
    }

    /**
     * Check that operands have the same type
     * Then checks that binary operation can be applied to operands of such types
     * @throws SemanticException whenever any of those checks fails
     */
    override fun check() {
        if (!typesAreEqual(left.type, right.type)) {
            semanticErrorMessage
                .operandTypeMismatch(left.type, right.type)
                .appendCustomErrorMessage("Binary operation cannot be executed correctly")
                .buildAndPrint()
            throw SemanticException("Attempted to call binary operation $op on unequal types: ${left.type}, ${right.type}")
        }
        val operationTypeNotValid: Boolean
            = when (op) {
                MUL, DIV, MOD, ADD, BinOperator.SUB -> !typesAreEqual(left.type, WInt())
                GT, GEQ, LT, LEQ -> !typesAreEqual(left.type, WInt()) && !typesAreEqual(left.type, WChar())
                EQ, NEQ -> false
                AND, OR -> !typesAreEqual(left.type, WBool())
            }
        if (operationTypeNotValid) {
            semanticErrorMessage
                .binOpInvalidType(left.type)
                .buildAndPrint()
            throw SemanticException("Attempted to call binary operation $op on operands of invalid type: ${left.type} ")
        }
    }

    override fun toString(): String {
        return "$op\n" + "  (scope:$st)\n${left.toString().prependIndent(INDENT)}\n${
            right.toString().prependIndent(INDENT)
        }"
    }

    override val type: WAny
        get() = when (op) {
            MUL, DIV, MOD, ADD, BinOperator.SUB -> WInt()
            GT, GEQ, LT, LEQ -> WBool()
            EQ, NEQ -> WBool()
            AND, OR -> WBool()
        }
}

/**
 * The AST Node for Unary Operations
 **/
class UnaryOperation(
    override val st: SymbolTable,
    val operand: Expr,
    val op: UnOperator,
    parserCtx: ParserRuleContext,
) : Expr {

    private val semanticErrorMessage: SemanticErrorMessageBuilder
        = SemanticErrorMessageBuilder().provideStart(PositionedError(parserCtx))

    override fun check() {
        val typeIsIncorrect: Boolean = when(op) {
            NOT -> operand.type !is WBool
            ORD -> operand.type !is WChar
            LEN -> operand.type !is WArray
            CHR, UnOperator.SUB -> operand.type !is WInt
        }
        if (typeIsIncorrect) {
            semanticErrorMessage
                .unOpInvalidType(operand.type)
                .buildAndPrint()
            throw SemanticException("Attempted to call $op operation on invalid type: ${operand.type}")
        }
    }

    override fun toString(): String {
        return "$op\n" + "  (scope:$st)\n${operand.toString().prependIndent(INDENT)}"
    }

    override val type: WAny
        get() = when (op) {
            NOT -> WBool()
            ORD -> WInt()
            CHR -> WChar()
            LEN -> WInt()
            UnOperator.SUB -> WInt()
        }
}

/**
 * The AST Node for Declarations
 **/
class Declaration(
    override val st: SymbolTable,
    var decType: WAny,
    val identifier: String,
    val rhs: RHS,
    parserCtx: ParserRuleContext,
) : Stat {

    private val semanticErrorMessage: SemanticErrorMessageBuilder
        = SemanticErrorMessageBuilder().provideStart(PositionedError(parserCtx))

    init {
        check()
        st.declare(identifier, decType)
    }

    override fun check() {
        if (!typesAreEqual(decType, rhs.type)) {
            semanticErrorMessage
                .operandTypeMismatch(decType, rhs.type)
                .appendCustomErrorMessage(
                    "The type of variable $identifier and the evaluated expression do not match")
                .buildAndPrint()
            throw SemanticException("Attempted to declare variable $identifier of type $decType to ${rhs.type}")
        }
    }

    override fun toString(): String {
        return "Declaration:\n" +
                "  (scope:$st)\n${("of: $identifier").prependIndent(INDENT)}\n${
            ("to: $rhs").toString().prependIndent(INDENT)
        }"
    }
}

/**
 * The AST Node for Assignments
 **/
class Assignment(
    override val st: SymbolTable,
    val lhs: LHS,
    val rhs: RHS,
    parserCtx: ParserRuleContext
) : Stat {

    private val semanticErrorMessage: SemanticErrorMessageBuilder
        = SemanticErrorMessageBuilder().provideStart(PositionedError(parserCtx))

    init {
        check()
    }

    override fun check() {
        if (!typesAreEqual(lhs.type, rhs.type)) {
            semanticErrorMessage
                .operandTypeMismatch(lhs.type, rhs.type)
                .buildAndPrint()
            throw SemanticException("Cannot assign ${rhs.type} to ${lhs.type}")
        }
        when (lhs) {
            is IdentifierSet -> st.reassign(lhs.identifier, rhs.type)
            is ArrayElement -> {
                val indices: Array<WInt> = lhs.indices.map { it.type as? WInt
                        ?: run {
                            semanticErrorMessage
                                .arrayIndexInvalidType()
                                .buildAndPrint()
                            throw SemanticException("Non-int index in array ${it.type}")
                        }
                }.toTypedArray()
                st.reassign(lhs.identifier, indices, rhs.type)
            }
            is PairElement -> {
                // Make sure this is: fst <identifier> = blah. Otherwise invalid.
                if (lhs.expr !is IdentifierGet) {
                    semanticErrorMessage
                        .pairElementInvalidType()
                        .buildAndPrint()
                    throw SemanticException("Cannot refer to ${lhs.type} with fst/snd")
                }
                st.reassign(lhs.expr.identifier, lhs.first, rhs.type)
            }
        }
    }

    override fun toString(): String {
        return "Assignment:\n" + "  (scope:$st)\n${lhs.toString().prependIndent(INDENT)}\n${
            rhs.toString().prependIndent(INDENT)
        }"
    }
}

/**
 * The AST Node for Setting Identifiers
 **/
class IdentifierSet(
    override val st: SymbolTable,
    val identifier: String,
) : LHS {

    override fun toString(): String {
        return "IdentifierSet:\n" + "  (scope:$st)\n${("identifier: $identifier").prependIndent(INDENT)}\n${
            ("type: $type").prependIndent(INDENT)
        }"
    }

    override val type: WAny
        get() = st.get(identifier)
}

/**
* The AST Node for Getting Identifiers
**/
class IdentifierGet(
    override val st: SymbolTable,
    val identifier: String,
    parserCtx: ParserRuleContext
) : Expr {

    private val semanticErrorMessage: SemanticErrorMessageBuilder
        = SemanticErrorMessageBuilder().provideStart(PositionedError(parserCtx))

    init {
        check()
    }

    override fun check() {
        if (!typesAreEqual(st.get(identifier), type)) {
            semanticErrorMessage
                .operandTypeMismatch(st.get(identifier), type)
                .appendCustomErrorMessage(
                    "$identifier has a type which does not match with the type of the right hand side.")
                .buildAndPrint()
            throw SemanticException("Attempted to use variable of type ${st.get(identifier)} as $type")
        }
    }

    override fun toString(): String {
        return "IdentifierGet:\n" + "  (scope:$st)\n${("identifier: $identifier").prependIndent(INDENT)}\n${
            ("type: $type").prependIndent(INDENT)
        }"
    }

    override val type: WAny
        get() = st.get(identifier)
}

/**
 * The AST Node for Array Elements
 * @property identifier : is the variable name of the array
 * @property indices : List of child ASTs
 **/
class ArrayElement(
    override val st: SymbolTable,
    val identifier: String,
    val indices: Array<Expr>,
    parserCtx: ParserRuleContext,
) : LHS, Expr {

    private val semanticErrorMessage: SemanticErrorMessageBuilder
        = SemanticErrorMessageBuilder().provideStart(PositionedError(parserCtx))

    init {
        check()
    }

    /**
     *  Here the semantic analysis is conducted inside getter
     */
    override fun check() {
        this.type
    }

    override fun toString(): String {
        return "ArrayElem:\n" + "  (scope:$st)\n${
            ("array identifier: $identifier\nindex/ices:\n${
                (indices.map { e -> e.toString() }
                    .reduceOrNull { a, b -> "$a\n$b" } ?: "").prependIndent("  ")
            }").prependIndent(INDENT)
        }\n${
            ("type: $type").prependIndent(INDENT)
        }"
    }

    override val type: WAny
        get() = st.get(identifier, indices.map { e ->
            e.type as? WInt
                ?: run {
                    semanticErrorMessage
                        .arrayIndexInvalidType()
                        .buildAndPrint()
                    throw SemanticException("Cannot use non-int index for array, actual: ${e.type}")
                }
        }.toTypedArray())
}

/**
 * The AST Node for If then Statements
 **/
class IfThenStat(
    override val st: SymbolTable,
    val condition: Expr,
    val thenStat: Stat,
    val elseStat: Stat,
    parserCtx: ParserRuleContext,
) : Stat {

    private val semanticErrorMessage: SemanticErrorMessageBuilder
        = SemanticErrorMessageBuilder().provideStart(PositionedError(parserCtx))

    init {
        check()
    }

    override fun check() {
        if (condition.type !is WBool) {
            semanticErrorMessage
                .ifStatConditionHasNonBooleanType(condition.type)
                .buildAndPrint()
            throw SemanticException("If statement has non-bool condition, actual: ${condition.type}")
        }
        thenStat.check()
        elseStat.check()
    }

    override fun toString(): String {
        return "If-Then-Else:\n" + "  (scope:$st)\n${
            ("if:\n${
                condition.toString().prependIndent("   ")
            }").prependIndent(INDENT)
        }\n${
            ("then:\n${thenStat.toString().prependIndent("   ")}").prependIndent(INDENT)
        }\n${("else:\n${elseStat.toString().prependIndent("   ")}").prependIndent(INDENT)}"
    }
}

/**
 * The AST Node for While Statements
 **/
class WhileStat(
    override val st: SymbolTable,
    val condition: Expr,
    val doBlock: Stat,
    parserCtx: ParserRuleContext,
) : Stat {

    private val semanticErrorMessage: SemanticErrorMessageBuilder
        = SemanticErrorMessageBuilder().provideStart(PositionedError(parserCtx))

    init {
        check()
    }

    override fun check() {
        if (condition.type !is WBool) {
            semanticErrorMessage
                .whileStatConditionHasNonBooleanType(condition.type)
                .buildAndPrint()
            throw SemanticException("While loop has non-bool condition, actual: ${condition.type}")
        }
        doBlock.check()
    }

    override fun toString(): String {
        return "While-do:\n" + "  (scope:$st)\n${
            ("condition:\n${
                condition.toString().prependIndent("   ")
            }").prependIndent(INDENT)
        }\n${
            ("do:\n${doBlock.toString().prependIndent("   ")}").prependIndent(INDENT)
        }"
    }
}

/**
 * The AST Node for Read Statements
 **/
class ReadStat(
    override val st: SymbolTable,
    val lhs: LHS,
    parserCtx: ParserRuleContext,
) : Stat {

    private val semanticErrorMessage: SemanticErrorMessageBuilder
        = SemanticErrorMessageBuilder().provideStart(PositionedError(parserCtx))

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

/**
 * The AST Node for Print Statements
 **/
class PrintStat(
    override val st: SymbolTable,
    private val newlineAfter: Boolean,
    val expr: Expr
) : Stat {

    init {
        check()
    }

    override fun check() {
        expr.check()
    }

    override fun toString(): String {
        return "Print:\n" + "  (scope:$st)\n${
            ("withNewline: $newlineAfter").prependIndent(INDENT)
        }\n${
            ("Expr: $expr").prependIndent(INDENT)
        }"
    }
}

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

    val semanticErrorMessage: SemanticErrorMessageBuilder
        = SemanticErrorMessageBuilder().provideStart(PositionedError(parserCtx))

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
            ("${if (first) "FST" else "SND"
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

/**
 * The AST Node for Free Statements
 **/
class FreeStat(
    override val st: SymbolTable,
    val expr: Expr,
    parserCtx: ParserRuleContext,
) : Stat {

    private val semanticErrorMessage: SemanticErrorMessageBuilder
        = SemanticErrorMessageBuilder().provideStart(PositionedError(parserCtx))

    init {
        check()
    }

    /**
     * Ensures that the type of the expression to be freed is a pair
     */
    override fun check() {
        if (expr.type !is WPair) {
            semanticErrorMessage
                .freeNonPair()
                .buildAndPrint()
            throw SemanticException("This isn't C, you can't free a $expr")
        }
    }

    override fun toString(): String {
        return "Free:\n" + "  (scope:$st)\n${expr.toString().prependIndent(INDENT)}"
    }

}

/**
 * The AST Node for Exit Statements
 **/
class ExitStat(
    override val st: SymbolTable,
    private val expression: Expr,
    parserCtx: ParserRuleContext,
) : Stat {

    private val semanticErrorMessage: SemanticErrorMessageBuilder
        = SemanticErrorMessageBuilder().provideStart(PositionedError(parserCtx))

    init {
        check()
    }

    override fun check() {
        if (expression.type !is WInt) {
            semanticErrorMessage
                .nonIntExpressionExit(expression.type)
                .buildAndPrint()
            throw SemanticException("Cannot exit with non-int expression. Actual: ${expression.type}")
        }
    }

    override fun toString(): String {
        return "Exit:\n" + "  (scope:$st)\n${expression.toString().prependIndent(INDENT)}"
    }
}

/**
 * The AST Node for Skip Statements
 **/
class SkipStat(override val st: SymbolTable) : Stat {
    override fun toString(): String {
        return "Skip"
    }
}

/**
 * The AST Node for Return Statements
 **/
class ReturnStat(
    override val st: SymbolTable,
    val expression: Expr,
    parserCtx: ParserRuleContext,
) : Stat, Typed {

    private val semanticErrorMessage: SemanticErrorMessageBuilder
        = SemanticErrorMessageBuilder().provideStart(PositionedError(parserCtx))

    init {
        check()
    }

    override val type: WAny
        get() = expression.type

    /**
     * Checks the scope
     */
    override fun check() {
        if (st.isGlobal) {
            semanticErrorMessage
                .returnFromGlobalScope()
                .buildAndPrint()
            throw SemanticException("Cannot return out of global scope.")
        }
    }

    override fun toString(): String {
        return "Return:\n" + "  (scope:$st)\n${expression.toString().prependIndent(INDENT)}"
    }
}

/**
 * The AST Node for Join Statements
 **/
class JoinStat(
    override val st: SymbolTable,
    val first: Stat,
    val second: Stat,
) : Stat {
    override fun check() {
        first.check()
        second.check()
    }

    override fun toString(): String {
        return "$first\n$second"
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
fun checkReturnType(stat: Stat, expected: WAny) {
    when (stat) {
        is ReturnStat -> if (!typesAreEqual(stat.type, expected)) {
            throw SemanticException("Mismatching return type for function, expected: $expected, got: ${stat.type} ")
        }
        is JoinStat -> {
            checkReturnType(stat.first, expected)
            checkReturnType(stat.second, expected)
        }
        is IfThenStat -> {
            checkReturnType(stat.thenStat, expected)
            checkReturnType(stat.elseStat, expected)
        }
        is WhileStat -> checkReturnType(stat.doBlock, expected)
    }
}