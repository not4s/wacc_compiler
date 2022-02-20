package instructions

class POP(val reg: Register): WInstruction {

    override fun toString(): String {
        return "POP {" + reg + "}"
    }
}