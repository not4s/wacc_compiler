package ast.statement

import ast.*
import symbolTable.ParentRefSymbolTable
import symbolTable.SymbolTable
import waccType.WAny
import waccType.WInt
import waccType.WStruct

/**
 * The AST Node for Struct Declaration Statements
 **/
class StructDeclarationStat(
    override val st: SymbolTable,
    override val type: WAny,
    val identifier: String,
) : Stat, Typed {

    override fun toString(): String {
        return "Declaration:\n" +
                "  (scope:$st)\n${("of: $identifier with type $type").prependIndent(INDENT)}"
    }
}