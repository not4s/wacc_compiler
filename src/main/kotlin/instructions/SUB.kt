package instructions

data class SUB(
    val rDest: Register,
    val lhs: Register,
    val rhs: Operand2
) : WInstruction {

    private var flagsSet: Boolean = false

    override fun toString(): String {
        val sb = "SUB"
        if (flagsSet) sb.plus("S")
        return "$sb $rDest, $lhs, $rhs"
    }
}