package instructions.operations

import instructions.WInstruction
import instructions.misc.DataDeclaration
import instructions.misc.Register

data class POP(val reg: Register, val data: DataDeclaration?) : WInstruction {
    constructor(reg: Register) : this(reg, null)
    init {
        if (data != null) {
            data.spOffset -= 4
        }
    }

    override fun toString(): String {
        return "POP {$reg}"
    }
}