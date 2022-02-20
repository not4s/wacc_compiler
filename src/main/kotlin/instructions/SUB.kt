package instructions

class SUB(val rDest: Register,
          val lhs:   Register,
          val rhs:   Operand2): WInstruction {
    
    var flagsSet: Boolean = false

    override fun toString(): String {
        var sb = "SUB"
        if(flagsSet) sb.plus("S")
        return sb + " " + rDest + ", " + lhs + ", " + rhs
    }
}