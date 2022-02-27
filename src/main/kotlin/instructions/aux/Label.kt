package instructions.aux

import instructions.WInstruction

data class Label(val label: String) : WInstruction {

    override fun toString(): String {
        return "$label:"
    }
}