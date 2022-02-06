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
                values.map { e -> e.toString() }.reduceOrNull { a, b -> "$a $b" }
                    ?: ""
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
    override val type: WAny,
) : Expr {
    override fun check() {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        TODO("Not yet implemented")
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
    override fun check() {
        when (op) {
            MUL, DIV, MOD, ADD, BinOperator.SUB -> {
                assertEqualTypes(left.type, right.type); assertEqualTypes(left.type, WInt())
            }
            GT, GEQ, LT, LEQ -> {
                assertEqualTypes(left.type, right.type); assertEqualTypes(left.type, WInt())
            }
            EQ, NEQ -> assertEqualTypes(left.type, right.type)
            AND, OR -> {
                assertEqualTypes(left.type, right.type); assertEqualTypes(left.type, WBool())
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
                throw SemanticException("")
            }
            ORD -> if (operand.type !is WChar) {
                throw SemanticException("")
            }
            CHR, UnOperator.SUB -> if (operand.type !is WInt) {
                throw SemanticException("")
            }
            LEN -> if (operand.type !is WArray) {
                throw SemanticException("")
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
            NOT -> WInt()
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

    override fun check() {
        assertEqualTypes(decType, rhs.type)
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
    override fun check() {
        assertEqualTypes(lhs.type, lhs.type)
        st.reassign(TODO(), rhs.type)
    }

    override fun toString(): String {
        return "Assingment:\n" + "  (scope:$st)\n${lhs.toString().prependIndent(INDENT)}\n${
            rhs.toString().prependIndent(INDENT)
        }"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }

}

class Identifer(
    override val st: SymbolTable,
    val ident: String,
    override val type: WAny,
) : LHS, Expr {
    override fun check() {
        // Always valid.
    }

    override fun toString(): String {
        return "Identifier:\n" + "  (scope:$st)\n${("ident: $ident").prependIndent(INDENT)}\n${
            ("type: $type").prependIndent(INDENT)
        }"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }
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
                (indices.map { e -> e.toString() }.reduceOrNull { a, b -> "$a\n$b" }
                    ?: "").prependIndent("  ")
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

class PrintStat(override val st: SymbolTable, val newlineAfter: Boolean, val expr: Expr) : Stat {
    override fun check() {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "Print:\n" + "  (scope:$st)\n${("withNewline: $newlineAfter").prependIndent(INDENT)}\n${
            ("Expr: $expr").prependIndent(INDENT)
        }"
    }

    override fun evaluate(): WAny {
        TODO("Not yet implemented")
    }

}

class ExitStat(
    override val st: SymbolTable,
    val exp: Expr,
) : Stat {
    override fun check() {
        if (exp.type !is WInt) {
            throw SemanticException("")
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
    override fun check() {
        if (exp.type !is WInt) {
            throw SemanticException("")
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




















