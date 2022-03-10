package ast

import symbolTable.SymbolTable
import waccType.WArray
import waccType.WUnknown

/**
 * The AST Node for Array Literals
 **/
class ArrayLiteral(
    override val st: SymbolTable,
    val values: Array<Expr>
) : RHS {

    override val type: WArray
        get() = if (values.isEmpty()) WArray(WUnknown()) else WArray(values.first().type)

    override fun toString(): String {
        return "ArrayLiteral\n  (scope:$st)\n${
            ("type: $type\nelements: [${
                values.map { e -> e.toString() }.reduceOrNull { a, b -> "$a $b" } ?: ""
            }]").prependIndent(INDENT)
        }"
    }
}