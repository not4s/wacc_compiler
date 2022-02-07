package ast

import ast.BinOperator.*
import ast.UnOperator.*
import symbolTable.PointerSymbolTable
import symbolTable.SymbolTable
import utils.SemanticException
import waccType.*

val INDENT = "  | "

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

class Program(
    override val st: SymbolTable,
    val funcs: Array<WACCFunction>,
    val stat: Stat,
) : AST {
    override fun check() {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "DEFINED FUNCTIONS:\n${"-".repeat(20)}\n${
            funcs.map { f -> f.toString() }.reduceOrNull { a, b -> "$a\n$b" } ?: "\n"
        }\n${"-".repeat(20)}\nGLOBAL PROGRAM BLOCK:\n${"-".repeat(20)}\n$stat"
    }

}

class WACCFunction(
    override val st: SymbolTable,
    val ident: String,
    val params: Map<String, WAny>,
    val body: Stat,
    override val type: WAny, // return type
) : AST, Typed {
    init {
        check()
    }
    override fun check() {
        // TODO: Param checking?
        body.check()

    }

    override fun toString(): String {
        return "Function($type) $ident(${
            params.map { (id, t) -> "($t)$id" }.reduceOrNull { a, b -> "$a, $b" } ?: ""
        }):\n${body.toString().prependIndent(INDENT)}"
    }
}

class FunctionCall(
    override val st: SymbolTable,
    val ident: String,
    val params: Array<Expr>,
    override val type: WAny,
) : RHS {
    override fun check() {
        TODO("Not yet implemented")
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
    override val type: WArray,
) : Expr, RHS {
    override fun check() {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
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
        return "Declaration:\n" + "  (scope:$st)\n${("of: $ident").prependIndent(INDENT)}\n${
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
        assertEqualTypes(lhs.type, rhs.type)
        when (lhs) {
            is IdentifierSet -> st.reassign(lhs.ident, rhs.type)
            is ArrayElement -> {
                // Make sure Exprs are of type int.
                val indices: Array<WInt> = lhs.indices.map { e ->
                    if (e.type is WInt) {
                        e.type as WInt
                    } else {
                        throw SemanticException("Non-int index in array ${lhs.ident}")
                    }
                }.toTypedArray()
                st.reassign(lhs.ident, indices, rhs.type)
            }
            is PairElement -> TODO("NYI")
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
    override val type: WAny, // type of element referring to
) : LHS, Expr {
    override fun check() {
        TODO("Not yet implemented")
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

}

class IfThenStat(
    override val st: SymbolTable,
    val condition: Expr,
    val thenStat: Stat,
    val elseStat: Stat,
) : Stat {

    override fun check() {
        TODO("Not yet implemented")
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

    override fun check() {
        TODO("Not yet implemented")
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
    override val type: WAny,
) : LHS, RHS {
    override fun check() {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "Pair element:\n" + "  (scope:$st)\n${
            ("${
                if (first) {
                    "FST"
                } else {
                    "SND"
                }
            }:\n${expr.toString().prependIndent(INDENT)}").prependIndent(INDENT)
        }"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }

}

class FreeStat(
    override val st: SymbolTable,
    val expr: Expr,
) : Stat {
    override fun check() {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
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
) : Stat {
    init {
        check()
    }

    override fun check() {
        if (exp.type !is WInt) {
            throw SemanticException("Cannot return with non-int expression, actual : ${exp.type}")
        }
        // Check scope
        if (st.isGlobal()) {
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


fun main() {
    val st = PointerSymbolTable()
    st.declare("x", WInt())
    val temp = Declaration(st, WInt(), "x", Literal(st, WInt()))
    temp.check()
}




















