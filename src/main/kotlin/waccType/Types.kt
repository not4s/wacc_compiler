package waccType



interface WAny

class WInt : WAny
class WStr : WAny
class WBool : WAny
class WChar : WAny

class WArray(val elemType : WAny) : WAny

class WPair(val leftType : WAny, rightType : WAny) : WAny

class WFunc(val params: Array<WAny>, val returnType : WAny) : WAny

fun typesAreEqual(x : WAny, y : WAny) : Boolean {
    return (x::class == y::class)
}


