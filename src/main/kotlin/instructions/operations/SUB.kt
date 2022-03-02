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
        val command = "SUB" + if (flagSet) "S" else ""
        return "$command $rDest, $lhs, $rhs"
    }
}