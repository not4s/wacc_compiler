package instructions

class Register {

    val rName: String

    constructor(name: String) {
        this.rName = name
    }

    override fun hashCode(): Int {
        return rName.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        when {
            other is Register -> return other.rName.equals(this.rName)
            other is Operand2 -> {
                val reg2     = other.getReg()
                return reg2 != null && reg2.equals(this)
            }
            else -> return false
        }
    }

    override fun toString(): String {
        when {
            rName.equals("r13") -> return "sp"
            rName.equals("r15") -> return "pc"
            else                -> return rName
        }
    }
}