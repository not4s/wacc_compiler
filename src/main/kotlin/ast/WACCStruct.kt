package ast

import symbolTable.SymbolTable
import waccType.WAny
import waccType.WStruct

class WACCStruct(
    override val st: SymbolTable,
    override val identifier: String,
    val elements: Map<String, WAny>
) : AST, WStruct(identifier), RHS {
    override fun toString(): String {
        return "struct $identifier{${
            elements.map { (id, t) -> "$t $id" }.reduceOrNull { a, b -> "$a, $b" } ?: throw Exception(
                "Cannot have no elements in a struct, this should have been handled during syntax analysis"
            )
        }}"
    }

    fun elemType(elem: String) = elements[elem]

    override fun equals(other: Any?): Boolean {
        if (other !is WACCStruct && other !is WStruct) {
            return false
        }
        return (other as WStruct).identifier == this.identifier
    }

    override fun hashCode(): Int {
        return identifier.hashCode()
    }

    override val type: WAny
        get() = WStruct(identifier)
}

class WACCStructElem(
    identifier: String,
    val elems: List<String>,
    override val st: SymbolTable,
    override val type: WAny
) : LHS, WStruct(identifier), Expr {
    override fun toString(): String {
        return "($type) $identifier.${elems.reduce { a, b -> "$a.$b" }}"
    }
}