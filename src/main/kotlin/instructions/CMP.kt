package instructions

class CMP(val reg: Register,
          val op2: Operand2): WInstruction {

    override fun toString(): String {
        return "CMP " + reg + ", " + op2
    }
}