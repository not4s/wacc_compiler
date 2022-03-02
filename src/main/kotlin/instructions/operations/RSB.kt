package instructions.operations

import instructions.WInstruction
import instructions.misc.Operand2
import instructions.misc.Register

data class NEG(
    val rDest: Register,
    val lhs: Register,
) : WInstruction {

    override fun toString(): String {
        return "NEG $rDest, $lhs"
    }
}