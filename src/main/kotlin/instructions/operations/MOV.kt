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
        LT, LE, EQ, GT, GE, NE
    }

    override fun toString(): String {
        var condName = ""
        if(cond != null) condName = cond.name
        return "MOV${condName} $rDest, $op2"
    }
}