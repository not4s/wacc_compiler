package instructions.misc

data class Register(val rName: String) : Operand2 {

    constructor(num: Int) : this("r$num")

    override fun toString(): String {
        return when (rName) {
            "r13" -> "sp"
            "r14" -> "lr"
            "r15" -> "pc"
            else -> rName
        }
    }

    companion object {
        fun linkRegister(): Register {
            return Register("lr")
        }
        fun stackPointer(): Register {
            return Register("sp")
        }
        fun programCounter(): Register {
            return Register("pc")
        }
        fun resultRegister(): Register {
            return Register("r0")
        }
    }
}