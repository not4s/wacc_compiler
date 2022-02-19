package ast.statement

import ast.INDENT
import ast.LHS
import ast.Stat
import symbolTable.SymbolTable

/**
 * The AST Node for Read Statements
 **/
class ReadStat(
    override val st: SymbolTable,
    private val lhs: LHS,
) : Stat {

    override fun toString(): String {
        return "Read:\n" + "  (scope:$st)\n${"LHS:\n${lhs.toString().prependIndent(INDENT)}"}"
    }
}