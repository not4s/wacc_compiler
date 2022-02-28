package instructions.misc

import instructions.WInstruction

class LTORG : WInstruction {
    override fun toString(): String {
        return ".ltorg"
    }
}

class BlankLine : WInstruction {
    override fun toString(): String {
        return ""
    }
}