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
}

data class ImmediateChar(val value: Char) : Operand2 {
    override fun toString(): String {
        return "#'$value'"
    }
}

data class ShiftedRegister(val reg: Register, val value: Int): Operand2 {
    override fun toString(): String {
        return "$reg, ASR #$value"
    }   
}

data class LabelReference(val name: String) : Loadable {

    constructor(literal: String, data: DataDeclaration) : this(data.getSymbolFromLiteral(literal))

    override fun toString(): String {
        return "=$name"
    }
}