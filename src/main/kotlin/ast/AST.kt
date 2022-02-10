package ast

import ast.BinOperator.*
import ast.UnOperator.*
import symbolTable.SymbolTable
import utils.ExitCode
import utils.SemanticException
import waccType.*
import kotlin.system.exitProcess

const val INDENT = "  | "

interface AST {
    val st: SymbolTable
    fun check() // must throw exceptions if semantic errors are found
    override fun toString(): String
}

interface Evaluable : AST {
    fun evaluate(): WAny
}

interface Typed : AST {
    val type: WAny
}

interface RHS : AST, Evaluable, Typed

interface LHS : AST, Typed

interface Expr : AST, Evaluable, Typed, RHS

interface Stat : AST, Evaluable

enum class BinOperator {
    MUL, DIV, MOD, ADD, SUB, GT, GEQ, LT, LEQ, EQ, NEQ, AND, OR;
}

enum class UnOperator {
    NOT, ORD, CHR, LEN, SUB;
}

class WACCFunction(
    override val st: SymbolTable,
    val ident: String,
    val params: Map<String, WAny>,
    val body: Stat,
    override val type: WAny, // return type
) : AST, Typed, WAny {
    override fun check() {
        // TODO: Param checking?
        body.check()
        if (!hasReturn(body, true)) {
            println("Function $ident does not return on every branch.")
            exitProcess(ExitCode.SYNTAX_ERROR)
        }
        checkReturnType(body, type)

    }

    override fun toString(): String {
        return "Function($type) $ident(${
            params.map { (id, t) -> "($t)$id" }.reduceOrNull { a, b -> "$a, $b" } ?: ""
        }):\n${"   ".prependIndent(INDENT)}"
    }
}

class FunctionCall(
    override val st: SymbolTable,
    val ident: String,
    val params: Array<Expr>,
) : RHS {
    init {
        check()
    }

    override fun check() {
        // Check params against st
        val func = st.get(ident) as WACCFunction
        // If function is not yet defined, just return
        if (func.body is SkipStat && func.params.isEmpty() && func.type is WUnknown) {
            return
        }
        if (func.params.size != params.size) {
            throw SemanticException("Argument count does not match up with expected count for function $ident")
        }
        func.params.onEachIndexed { i, (_, v) ->
            if (!typesAreEqual(v, params[i].type)) {
                throw SemanticException("Mismatching types for function $ident call: expected $v, got ${params[i].type}")
            }
        }
    }

    override fun toString(): String {
        return "Calling $ident with parameters...:\n${
            params.mapIndexed { i, e ->
                "Parameter $i:\n${
                    e.toString().prependIndent(INDENT)
                }".prependIndent("  ")
            }.reduceOrNull { a, b -> "$a\n$b" } ?: ""
        }"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }

    override val type: WAny
        get() = (st.get(ident) as WACCFunction).type
}

class Literal(
    override val st: SymbolTable,
    override val type: WBase,
) : Expr, RHS {
    override fun check() {}

    override fun toString(): String {
        return "Literal\n  (scope:$st)\n${("type: $type").prependIndent(INDENT)}"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }
}

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
            ("type: $type\nelems: [${
                values.map { e -> e.toString() }.reduceOrNull { a, b -> "$a $b" } ?: ""
            }]").prependIndent(INDENT)
        }"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }
}

class WACCType(override val st: SymbolTable, override val type: WAny) : Typed {
    override fun check() {

    }

    override fun toString(): String {
        return "WACCType: $type"
    }
}

class PairLiteral(
    override val st: SymbolTable,
    override val type: WPair,
) : Expr {
    override fun check() {
    }

    override fun toString(): String {
        return "PairLiteral(null)\n  (scope:$st)\n${("type: $type").prependIndent(INDENT)}"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }
}

class NewPairRHS(
    override val st: SymbolTable,
    val left: Expr,
    val right: Expr,
    override val type: WPair,
) : RHS {
    override fun check() {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "NEWPAIR:\n  (scope:$st)\nleft:\n${
            left.toString().prependIndent(INDENT)
        }\nright:\n" + right.toString().prependIndent(INDENT)
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }
}

class BinaryOperation(
    override val st: SymbolTable,
    val left: Expr,
    val right: Expr,
    val op: BinOperator,
) : Expr {
    init {
        check()
    }

    override fun check() {
        when (op) {
            MUL, DIV, MOD, ADD, BinOperator.SUB -> {
                if (!typesAreEqual(left.type, right.type)) {
                    throw SemanticException("Attempted to call binary operation $op on unequal types: ${left.type}, ${right.type}")
                }
                if (!typesAreEqual(left.type, WInt())) {
                    throw SemanticException("Attempted to call binary operation $op on non-int types: ${left.type} ")
                }
            }
            GT, GEQ, LT, LEQ -> {
                if (!typesAreEqual(left.type, right.type)) {
                    throw SemanticException("Attempted to call binary operation $op on unequal types: ${left.type}, ${right.type}")
                }
                if (!typesAreEqual(left.type, WInt()) && !typesAreEqual(left.type, WChar())) {
                    throw SemanticException("Attempted to call binary operation $op on weird (non-int, non-char) types: ${left.type} ")
                }
            }
            EQ, NEQ -> {
                if (!typesAreEqual(left.type, right.type)) {
                    throw SemanticException("Attempted to call binary operation $op on unequal types: ${left.type}, ${right.type}")
                }
            }
            AND, OR -> {
                if (!typesAreEqual(left.type, right.type)) {
                    throw SemanticException("Attempted to call binary operation $op on unequal types: ${left.type}, ${right.type}")
                }
                if (!typesAreEqual(left.type, WBool())) {
                    throw SemanticException("Attempted to call binary operation $op on non-bool types: ${left.type} ")
                }
            }
        }
    }

    override fun toString(): String {
        return "$op\n" + "  (scope:$st)\n${left.toString().prependIndent(INDENT)}\n${
            right.toString().prependIndent(INDENT)
        }"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }

    override val type: WAny
        get() = when (op) {
            MUL, DIV, MOD, ADD, BinOperator.SUB -> WInt()
            GT, GEQ, LT, LEQ -> WBool()
            EQ, NEQ -> WBool()
            AND, OR -> WBool()
        }
}

class UnaryOperation(
    override val st: SymbolTable,
    val operand: Expr,
    val op: UnOperator,
) : Expr {
    override fun check() {
        when (op) {
            NOT -> if (operand.type !is WBool) {
                throw SemanticException("Attempted to call $op on non-bool type: ${operand.type}")
            }
            ORD -> if (operand.type !is WChar) {
                throw SemanticException("Attempted to call $op on non-char type: ${operand.type}")
            }
            CHR, UnOperator.SUB -> if (operand.type !is WInt) {
                throw SemanticException("Attempted to call $op on non-int type: ${operand.type}")
            }
            LEN -> if (operand.type !is WArray) {
                throw SemanticException("Attempted to call $op on non-array type: ${operand.type}")
            }
        }
    }

    override fun toString(): String {
        return "$op\n" + "  (scope:$st)\n${operand.toString().prependIndent(INDENT)}"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
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

class Declaration(
    override val st: SymbolTable,
    val decType: WAny,
    val ident: String,
    val rhs: RHS,
) : Stat {
    init {
        check()
        st.declare(ident, decType)
    }

    override fun check() {
        if (!typesAreEqual(decType, rhs.type)) {
            throw SemanticException("Attempted to declare variable $decType $ident to ${rhs.type}")
        }
    }

    override fun toString(): String {
        return "Declaration:\n" +
                "  (scope:$st)\n${("of: $ident").prependIndent(INDENT)}\n${
            ("to: $rhs").toString().prependIndent(INDENT)
        }"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }
}

class Assignment(
    override val st: SymbolTable,
    val lhs: LHS,
    val rhs: RHS,
) : Stat {
    init {
        check()
    }

    override fun check() {
        if (!typesAreEqual(lhs.type, rhs.type)) {
            throw SemanticException("Cannot assign ${rhs.type} to ${lhs.type}")
        }
        when (lhs) {
            is IdentifierSet -> st.reassign(lhs.ident, rhs.type)
            is ArrayElement -> {
                val indices: Array<WInt> = lhs.indices.map { it.type as? WInt
                        ?: throw SemanticException("Non-int index in array ${lhs.ident}")
                }.toTypedArray()
                st.reassign(lhs.ident, indices, rhs.type)
            }
            is PairElement -> {
                // Make sure this is: fst <ident> = blah. Otherwise invalid.
                if (lhs.expr !is IdentifierGet) {
                    throw SemanticException("Cannot refer to ${lhs.type} with fst/snd")
                }
                st.reassign(lhs.expr.ident, lhs.first, rhs.type)
            }
        }
    }

    override fun toString(): String {
        return "Assignment:\n" + "  (scope:$st)\n${lhs.toString().prependIndent(INDENT)}\n${
            rhs.toString().prependIndent(INDENT)
        }"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }
}

class IdentifierSet(
    override val st: SymbolTable,
    val ident: String,
) : LHS {
    override fun check() {
        // Always valid.
    }

    override fun toString(): String {
        return "IdentifierSet:\n" + "  (scope:$st)\n${("ident: $ident").prependIndent(INDENT)}\n${
            ("type: $type").prependIndent(INDENT)
        }"
    }

    override val type: WAny
        get() = st.get(ident)
}

class IdentifierGet(
    override val st: SymbolTable,
    val ident: String,
) : Expr {
    init {
        check()
    }

    override fun check() {
        if (!typesAreEqual(st.get(ident), type)) {
            throw SemanticException("Attempted to use variable of type ${st.get(ident)} as $type")
        }
    }

    override fun toString(): String {
        return "IdentifierGet:\n" + "  (scope:$st)\n${("ident: $ident").prependIndent(INDENT)}\n${
            ("type: $type").prependIndent(INDENT)
        }"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }

    override val type: WAny
        get() = st.get(ident)
}

class ArrayElement(
    override val st: SymbolTable,
    val ident: String, // name of array
    val indices: Array<Expr>, // List of indices
) : LHS, Expr {
    init {
        check()
    }

    override fun check() {
        this.type // call getter
    }

    override fun toString(): String {
        // This string also summons Cthulhu
        return "ArrayElem:\n" + "  (scope:$st)\n${
            ("array ident: $ident\nindex/ices:\n${
                (indices.map { e -> e.toString() }
                    .reduceOrNull { a, b -> "$a\n$b" } ?: "").prependIndent("  ")
            }").prependIndent(INDENT)
        }\n${
            ("type: $type").prependIndent(INDENT)
        }"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }

    override val type: WAny
        get() = st.get(ident, indices.map { e ->
            e.type as? WInt
                ?: throw SemanticException("Cannot use non-int index for array, actual: ${e.type}")
        }.toTypedArray())
}

class IfThenStat(
    override val st: SymbolTable,
    val condition: Expr,
    val thenStat: Stat,
    val elseStat: Stat,
) : Stat {
    init {
        check()
    }

    override fun check() {
        if (condition.type !is WBool) {
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

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }
}

class WhileStat(
    override val st: SymbolTable,
    val condition: Expr,
    val doBlock: Stat,
) : Stat {
    init {
        check()
    }

    override fun check() {
        if (condition.type !is WBool) {
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

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }
}

class ReadStat(
    override val st: SymbolTable,
    val lhs: LHS,
) : Stat {
    init {
        check()
    }

    override fun check() {
        if (lhs.type !is WChar && lhs.type !is WInt) {
            throw SemanticException("Cannot read into non-char or non-int variable, actual: ${lhs.type}")
        }
    }

    override fun toString(): String {
        return "Read:\n" + "  (scope:$st)\n${"LHS:\n${lhs.toString().prependIndent(INDENT)}"}"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }

}

class PrintStat(override val st: SymbolTable, val newlineAfter: Boolean, val expr: Expr) : Stat {
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

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }

}

class PairElement(
    override val st: SymbolTable,
    val first: Boolean, // true = fst, false = snd
    val expr: Expr,
) : LHS, RHS {
    init {
        check()
    }
    override fun check() {
        if (expr.type !is WPair) {
            throw SemanticException("Cannot call fst/snd on non-pair type: ${expr.type}")
        }
        // Check null
        if (expr is PairLiteral) {
            throw SemanticException("NULL POINTER EXCEPTION! Can't deref null.")
        }
    }

    override fun toString(): String {
        return "Pair element:\n" + "  (scope:$st)\n${
            ("${if (first) "FST" else "SND"
            }:\n${expr.toString().prependIndent(INDENT)}").prependIndent(INDENT)
        }"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }

    override val type: WAny
        get() {
            val pair = expr.type as WPair
            return if (first) pair.leftType else pair.rightType
        }
}

class FreeStat(
    override val st: SymbolTable,
    val expr: Expr,
) : Stat {
    init {
        check()
    }

    override fun check() {
        // Make sure expr is Pair
        if (expr.type !is WPair) {
            throw SemanticException("This isn't C, you can't free a $expr")
        }
    }

    override fun toString(): String {
        return "Free:\n" + "  (scope:$st)\n${expr.toString().prependIndent(INDENT)}"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }

}

class ExitStat(
    override val st: SymbolTable,
    val exp: Expr,
) : Stat {
    init {
        check()
    }

    override fun check() {
        if (exp.type !is WInt) {
            throw SemanticException("Cannot exit with non-int expression. Actual: ${exp.type}")
        }
    }

    override fun toString(): String {
        return "Exit:\n" + "  (scope:$st)\n${exp.toString().prependIndent(INDENT)}"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }
}

class SkipStat(override val st: SymbolTable) : Stat {
    override fun check() {
        // Always succeeds
    }

    override fun toString(): String {
        return "Skip"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }
}

class ReturnStat(
    override val st: SymbolTable,
    val exp: Expr,
) : Stat, Typed {
    init {
        check()
    }

    override val type: WAny
        get() = exp.type

    override fun check() {
        // Check scope
        if (st.isGlobal) {
            throw SemanticException("Cannot return out of global scope.")
        }
    }

    override fun toString(): String {
        return "Return:\n" + "  (scope:$st)\n${exp.toString().prependIndent(INDENT)}"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }
}

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

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }
}

fun hasReturn(stat: Stat, inOuterFuncScope: Boolean): Boolean {
    return when (stat) {
        is ReturnStat -> true
        is ExitStat -> true
        is JoinStat -> if (hasReturn(stat.first, true) && !hasReturn(stat.second, false) && inOuterFuncScope) {
            println("Should not have return before another non-return statement."); exitProcess(ExitCode.SYNTAX_ERROR)
        } else {
            hasReturn(stat.second, true)
        }
        is IfThenStat -> hasReturn(stat.thenStat, false) && hasReturn(stat.elseStat, false)
        is WhileStat -> hasReturn(stat.doBlock, false)
        else -> false
    }
}

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