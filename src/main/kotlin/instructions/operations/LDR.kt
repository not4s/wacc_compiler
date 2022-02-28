package instructions.operations

import instructions.WInstruction
import instructions.misc.Register

/**
 * Indicator that the operand can be the field of LDR source
 */
interface Loadable

data class LDR(
    val rDest: Register,
    val src: Loadable,
    var isSignedByte: Boolean = false
) : WInstruction {
    override fun toString(): String {
        val instruction = "LDR" + if (isSignedByte) "SB" else ""
        return "$instruction $rDest, $src"
    }
}