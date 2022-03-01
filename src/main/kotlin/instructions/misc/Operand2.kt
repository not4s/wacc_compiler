package instructions.misc

interface Operand2

/**
 * Indicator that the operand can be the field of LDR source
 */
interface Loadable : Operand2

data class LoadImmediate(val value: Int) : Loadable {
    override fun toString(): String {
        return "=$value"
    }
}

data class Immediate(val value: Int) : Operand2 {
    override fun toString(): String {
        return "#$value"
    }
    fun asLoadable(): LoadImmediate = LoadImmediate(value)
}

data class ImmediateChar(val value: Char) : Operand2 {
    override fun toString(): String {
        return "#'$value'"
    }
}

data class shiftedRegister(val reg: Register, val value: Int): Operand2 {
    override fun toString(): String {
        return "$reg, #$value"
    }   
}

data class LabelReference(val name: String) : Loadable {
    override fun toString(): String {
        return "=$name"
    }
}