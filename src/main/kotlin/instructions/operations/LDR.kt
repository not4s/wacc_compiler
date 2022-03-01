package instructions.operations

import instructions.WInstruction
import instructions.misc.Loadable
import instructions.misc.Register

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