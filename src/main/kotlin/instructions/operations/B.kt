package instructions.operations

import instructions.WInstruction

data class B(
    val label: String,
    var link: Boolean = false,
    val cond: Condition? = null
) : WInstruction {

    enum class Condition {
        L, EQ, NE
    }

    override fun toString(): String {
        var sb = "B"
        if(link) sb += "L"
        if(cond != null) sb += cond.name
        return sb + " ${label}"
    }
}