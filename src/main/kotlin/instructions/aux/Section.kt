package instructions.aux

import instructions.WInstruction

data class Section(val name: String) : WInstruction {

    private var arguments = listOf<String>()

    constructor(label: String, vararg args: String) : this(label) {
        this.arguments = args.toList()
    }

    override fun toString(): String {
        val args = arguments.takeIf { it.isNotEmpty() }?.joinToString(", ")?.let { " $it" } ?: ""
        return "$name$args"
    }
}