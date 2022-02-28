package instructions.misc

import instructions.operations.Loadable

interface Operand2 : Loadable

class Immediate(val value: Int) : Operand2 {
    override fun toString(): String {
        return "=$value"
    }
}
class ImmediateHash(val value: Int) : Operand2 {
    override fun toString(): String {
        return "#$value"
    }
}