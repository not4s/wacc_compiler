package instructions

data class PUSH(val reg: Register) : WInstruction {

    override fun toString(): String {
        return "PUSH {$reg}"
    }
}