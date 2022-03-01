package instructions.operations

import instructions.WInstruction
import instructions.misc.Operand2
import instructions.misc.Register

data class SMULL(
    val rHigh: Register,
    val rLow: Register,
    val op1: Register,
    val op2: Register,
) : WInstruction {

    override fun toString(): String {
        return "SMULL ${rHigh}, ${rLow}, ${op1}, ${op2}"
    }
}