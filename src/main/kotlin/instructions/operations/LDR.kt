package instructions.operations

import instructions.WInstruction
import instructions.misc.ConditionCode
import instructions.misc.Loadable
import instructions.misc.Register

data class LDR(
    val rDest: Register,
    val src: Loadable,
    var isSignedByte: Boolean = false,
    val conditionCode: ConditionCode? = null,
) : WInstruction {
    override fun toString(): String {
        val instruction = "LDR${conditionCode ?: ""}" + if (isSignedByte) "SB" else ""
        return "$instruction $rDest, ${
            if (src is Register) {
                "[${src}]"
            } else {
                "$src"
            }
        } "
    }
}