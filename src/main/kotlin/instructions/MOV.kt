package instructions

data class MOV(
    val rDest: Register,
    val op2: Operand2,
    val cond: Condition? = null
) : WInstruction {

    enum class Condition {
        LT, LTE, EQ, GT, GTE, NE
    }

    override fun toString(): String {
        val sb = "MOV"
        if (cond != null) sb.plus(cond)
        sb.plus(" $rDest, $op2")
        return sb
    }
}