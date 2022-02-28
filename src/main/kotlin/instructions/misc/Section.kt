package instructions.misc

import instructions.WInstruction

open class Section(val name: String) : WInstruction {

    protected var arguments = listOf<String>()

    constructor(name: String, vararg args: String) : this(name) {
        this.arguments = args.toList()
    }

    override fun toString(): String {
        val args = arguments.takeIf { it.isNotEmpty() }?.joinToString(", ")?.let { " $it" } ?: ""
        return "$name$args"
    }
}

class SubSection(name: String): Section(name) {
    constructor(name: String, vararg args: String) : this(name) {
        this.arguments = args.toList()
    }
}