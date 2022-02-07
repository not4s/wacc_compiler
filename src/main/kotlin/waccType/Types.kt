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
}

class WPair(val leftType: WAny, val rightType: WAny) : WAny {
    override fun toString(): String {
        return "Pair($leftType, $rightType)"
    }
}

fun typesAreEqual(x: WAny, y: WAny): Boolean {
    return (x::class == y::class || x is WUnknown || y is WUnknown)
}

fun assertEqualTypes(x: WAny, y: WAny) {
    if (!typesAreEqual(x, y)) {
        throw SemanticException("")
    }
}


