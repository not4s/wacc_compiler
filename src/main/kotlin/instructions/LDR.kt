package instructions

class LDR(val rDest:        Register,
          val op2:          Operand2,
          var isSignedByte: Boolean = false): WInstruction {
    
    override fun toString(): String {
        var sb = "LDR"
        if(isSignedByte) sb.plus("SB")
        sb.plus(" " + rDest + ", " + op2)
        return sb
    }
}