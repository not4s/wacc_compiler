package ast.statement

import ast.Expr
import ast.INDENT
import ast.Stat
import symbolTable.SymbolTable

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
 * The AST Node for Skip Statements
 **/
class SkipStat(override val st: SymbolTable) : Stat {
    override fun toString(): String {
        return "Skip"
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