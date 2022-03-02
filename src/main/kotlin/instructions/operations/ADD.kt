package instructions.operations

import instructions.WInstruction
import instructions.misc.Operand2
import instructions.misc.Register

data class ADD(
    val rDest: Register,
    val rSrc: Register,
    val op2: Operand2,
    val shiftVal: Int? = null
) : WInstruction {

    var flagSet: Boolean = false

    override fun toString(): String {
        val command = "ADD" + if (flagSet) "S" else ""
        val shift = shiftVal?.let { ", LSL #$it" } ?: ""
        return "$command $rDest, $rSrc, $op2$shift"
    }
}