package instructions.operations

import instructions.WInstruction
import instructions.misc.Operand2
import instructions.misc.Register

data class SUB(
    val rDest: Register,
    val lhs: Register,
    val rhs: Operand2
) : WInstruction {

    var flagSet: Boolean = false

    override fun toString(): String {
        val sb = "SUB"
        if (flagSet) sb.plus("S")
        return "$sb $rDest, $lhs, $rhs"
    }
}