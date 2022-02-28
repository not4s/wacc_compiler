package instructions.operations

import instructions.WInstruction
import instructions.misc.Operand2
import instructions.misc.Register

data class MOV(
    val rDest: Register,
    val op2: Operand2,
    val cond: Condition? = null
) : WInstruction {

    enum class Condition {
        LT, LTE, EQ, GT, GTE, NE
    }

    override fun toString(): String {
        return "MOV${cond ?: ""} $rDest, $op2"
    }
}