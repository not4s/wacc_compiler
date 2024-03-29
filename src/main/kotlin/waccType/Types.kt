package waccType

import ast.WACCStruct

interface WAny

sealed interface WBase : WAny

/**
 * Used as a type for empty array literals
 */
object WUnknown : WAny, WBase {
    override fun toString(): String {
        return "Unknown"
    }
}

class WInt(
    val value: Int? = null
) : WBase {
    override fun toString(): String {
        return "Int($value)"
    }
}

class WStr(
    val value: String? = null
) : WBase {
    override fun toString(): String {
        return "String(\"${value}\")"
    }
}

class WBool(
    val value: Boolean? = null
) : WBase {
    override fun toString(): String {
        return "Bool(${value})"
    }
}

class WChar(
    val value: Char? = null
) : WBase {
    override fun toString(): String {
        return "Char(\'${value}\')"
    }
}

class WArray(val elemType: WAny) : WAny {
    override fun toString(): String {
        return "${elemType}[]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WArray

        if (!typesAreEqual(elemType, other.elemType)) return false

        return true
    }

    override fun hashCode(): Int {
        return elemType.hashCode()
    }

}

class WPair(
    val leftType: WAny,
    val rightType: WAny
) : WAny {

    override fun toString(): String {
        return "Pair($leftType, $rightType)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WPair

        if (!typesAreEqual(leftType, other.leftType)) return false
        if (!typesAreEqual(rightType, other.rightType)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = leftType.hashCode()
        result = 31 * result + rightType.hashCode()
        return result
    }

    companion object {
        fun ofWUnknowns(): WPair {
            return WPair(WUnknown, WUnknown)
        }
    }
}

open class WStruct(open val identifier : String) : WAny {
    override fun toString(): String {
        return "struct $identifier"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is WACCStruct && other !is WStruct) {
            return false
        }
        return (other as WStruct).identifier == this.identifier
    }
}

/**
 * Used to check type validity when keyword 'pair' is used in variable declaration
 */
abstract class IncompleteWPair : WAny {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

/**
 * Used to check type validity when keyword 'pair' is used in variable declaration
 */
class WPairKW : IncompleteWPair() {

    override fun toString(): String {
        return "pair"
    }
}

/**
 * Used to check type validity when keyword 'pair' is used in variable declaration
 */
class WPairNull : IncompleteWPair() {

    override fun toString(): String {
        return "null"
    }
}

/**
 * For pairs this function allows subclasses of IncompleteWPair,
 * such as WPairKW and WPairNull have the same type as complete WPair
 */
fun typesAreEqual(x: WAny, y: WAny): Boolean {
    if (x is WPair && y is IncompleteWPair
        || y is WPair && x is IncompleteWPair
        || x is IncompleteWPair && y is IncompleteWPair
    ) {
        return true
    }
    return if (x !is WArray && x !is WPair) {
        (x::class == y::class || x is WUnknown || y is WUnknown || x == y)
    } else {
        (x::class == y::class && x == y) || y is WUnknown
    }
}
