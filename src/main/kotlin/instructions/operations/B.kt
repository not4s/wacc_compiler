package instructions.operations

import instructions.WInstruction

data class B(
    val label: String,
    val cond: Condition? = null
) : WInstruction {

    enum class Condition {
        L, EQ
    }

    override fun toString(): String {
        return "B${cond ?: ""} $label"
    }
}