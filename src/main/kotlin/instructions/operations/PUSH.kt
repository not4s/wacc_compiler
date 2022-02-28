package instructions.operations

import instructions.WInstruction
import instructions.misc.Register

data class PUSH(val reg: Register) : WInstruction {

    override fun toString(): String {
        return "PUSH {$reg}"
    }
}