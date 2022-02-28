package instructions.operations

import instructions.WInstruction
import instructions.misc.Operand2
import instructions.misc.Register

data class CMP(
    val reg: Register,
    val op2: Operand2
) : WInstruction {

    override fun toString(): String {
        return "CMP $reg, $op2"
    }
}