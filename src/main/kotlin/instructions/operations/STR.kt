package instructions.operations

import instructions.WInstruction
import instructions.misc.Register

data class STR(
    val rSrc: Register,
    val rDest: Register,
    val offset: Int = 0,
    var isSignedByte: Boolean = false
) : WInstruction {

    private var regWriteBack: Boolean = false

    override fun toString(): String {
        var sb = "STR"
        if (isSignedByte) sb += ("B")
        sb += (" $rSrc, [$rDest")
        sb += when {
            offset != 0 -> (", #$offset]")
            else -> ("]")
        }
        if (regWriteBack) sb +=("!")
        return sb
    }
}