package instructions.aux

import instructions.WInstruction

class LTORG : WInstruction {
    override fun toString(): String {
        return ".ltorg"
    }
}