package instructions.operations

import instructions.WInstruction
import instructions.aux.Register

data class POP(val reg: Register) : WInstruction {

    override fun toString(): String {
        return "POP {$reg}"
    }
}