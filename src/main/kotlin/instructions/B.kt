package instructions

class B(val label: String,
        val cond:  Condition? = null): WInstruction {

    enum class Condition {
        L, EQ
    }

    override fun toString(): String {
        var sb = "B"
        if(cond != null) sb.plus(cond)
        sb.plus(" " + label)
        return sb
    }
}