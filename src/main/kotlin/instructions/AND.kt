package instructions

import instructions.aux.Operand2
import instructions.aux.Register

data class AND(
    val rDest: Register,
    val lhs: Register,
    val rhs: Operand2
) : WInstruction {

    override fun toString(): String {
        return "AND $rDest, $lhs, $rhs"
    }
}