package instructions.misc

enum class Register: Operand2, Loadable {
    // General Registers, Result is generally stored in R0
    R0, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12,
    // R13 = Stack Pointer, R14 = Link Register, R15 = Program Counter
    SP, LR, PC;

    override fun toString(): String {
        return name.lowercase()
    }
}