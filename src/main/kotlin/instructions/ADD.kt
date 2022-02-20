package instructions

class ADD(val rDest: Register,
          val rSrc:  Register,
          val op2:   Operand2,
          val shiftVal: Int? = null): WInstruction {

    var flagSet: Boolean = false

    override fun toString(): String {
        var sb = "ADD"
        if(flagSet) sb.plus("S")
        sb.plus(" " + rDest + ", " + rSrc + ", " + op2)
        if(shiftVal != null) sb.plus(", LSL #" + shiftVal)
        return sb
    }
}