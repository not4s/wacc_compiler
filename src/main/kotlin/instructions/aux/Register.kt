package instructions.aux

data class Register(val rName: String) {

    override fun hashCode(): Int {
        return rName.hashCode()
    }

//    override fun equals(other: Any?): Boolean {
//        return when (other) {
//            is Register -> other.rName == this.rName
//            is Operand2 -> {
//                val reg2 = other.getReg()
//                reg2 != null && reg2 == this
//            }
//            else -> false
//        }
//    }

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
    }
}