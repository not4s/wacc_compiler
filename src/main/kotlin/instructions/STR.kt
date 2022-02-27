package instructions

import instructions.aux.Register

data class STR(
    val rSrc: Register,
    val rDest: Register,
    val offset: Int = 0,
    var isSignedByte: Boolean = false
) : WInstruction {

    private var regWriteBack: Boolean = false

    override fun toString(): String {
        val sb = "STR"
        if (isSignedByte) sb.plus("B")
        sb.plus(" $rSrc, [$rDest")
        when {
            offset != 0 -> sb.plus(", #$offset]")
            else -> sb.plus("]")
        }
        if (regWriteBack) sb.plus("!")
        return sb
    }
}