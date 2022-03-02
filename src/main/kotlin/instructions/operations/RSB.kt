package instructions.operations

import instructions.WInstruction
import instructions.misc.Operand2
import instructions.misc.Register

data class RSB(
    val rDest: Register,
    val lhs: Register,
) : WInstruction {

    override fun toString(): String {
        return "RSBS $rDest, $lhs, #0"
    }
}