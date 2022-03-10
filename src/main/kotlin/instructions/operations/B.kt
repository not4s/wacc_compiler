package instructions.operations

import instructions.WInstruction

data class B(
    val label: String,
    var link: Link = Link.LINK,
    val cond: Condition? = null
) : WInstruction {

    enum class Condition {
        // VS : true if Overflow
        EQ, NE, VS, LT, CS
    }

    override fun toString(): String {
        var sb = "B"
        if(link == Link.LINK) sb += "L"
        if(cond != null) sb += cond.name
        return "$sb $label"
    }
}

enum class Link {
    LINK, NO_LINK
}