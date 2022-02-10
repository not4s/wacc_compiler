package waccType

import utils.SemanticException


interface WAny

interface WBase : WAny

class WUnknown : WAny {
    override fun toString(): String {
        return "Unknown"
    }
}

class WInt : WBase {
    override fun toString(): String {
        return "Int"
    }
}

class WStr : WBase {
    override fun toString(): String {
        return "String"
    }
}

class WBool : WBase {
    override fun toString(): String {
        return "Bool"
    }
}

class WChar : WBase {
    override fun toString(): String {
        return "Char"
    }
}

class WArray(val elemType: WAny) : WAny {
    override fun toString(): String {
        return "$elemType[]"
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

class WPair(val leftType: WAny, val rightType: WAny) : WAny {
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

}

fun typesAreEqual(x: WAny, y: WAny): Boolean {
    return if (x !is WArray && x !is WPair) {
        (x::class == y::class || x is WUnknown || y is WUnknown)
    } else {
        (x::class == y::class && x == y || x is WUnknown || y is WUnknown)
    }
}

fun assertEqualTypes(x: WAny, y: WAny) {
    if (!typesAreEqual(x, y)) {
        throw SemanticException("")
    }
}


