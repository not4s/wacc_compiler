package instructions.aux

import instructions.Loadable

interface Operand2 : Loadable

class Immediate(val value: Int) : Operand2 {
    override fun toString(): String {
        return "=$value"
    }
}