package instructions

import instructions.aux.Register

data class PUSH(val reg: Register) : WInstruction {

    override fun toString(): String {
        return "PUSH {$reg}"
    }
}