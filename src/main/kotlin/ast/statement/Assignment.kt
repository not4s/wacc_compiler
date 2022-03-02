package ast.statement

import ast.*
import symbolTable.SymbolTable

/**
 * The AST Node for Assignments
 **/
class Assignment(
    override val st: SymbolTable,
    val lhs: LHS,
    val rhs: RHS
) : Stat {
    override fun toString(): String {
        return "Assignment:\n" + "  (scope:$st)\n${lhs.toString().prependIndent(INDENT)}\n${
            rhs.toString().prependIndent(INDENT)
        }"
    }
}