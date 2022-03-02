package instructions.operations

import instructions.WInstruction
import instructions.misc.Operand2
import instructions.misc.Register

data class AND(
    val rDest: Register,
    val lhs: Register,
    val rhs: Operand2
) : WInstruction {

    override fun toString(): String {
        return "AND $rDest, $lhs, $rhs"
    }
}