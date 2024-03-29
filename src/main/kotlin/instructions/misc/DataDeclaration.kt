package instructions.misc

import instructions.WInstruction

class DataDeclaration {

    private var noNameMsgCounter: Int = 0
    private val declarations = mutableMapOf<String, String>()
    var spOffset = 0
    fun addDeclaration(literal: String) : String {
        return if (declarations.values.contains(literal)) {
            // find the key corresponding to the value of the literal
            getSymbolFromLiteral(literal)
        } else {
            val key = "msg_${noNameMsgCounter++}"
            declarations[key] = literal
            key
        }
    }

    fun isEmpty(): Boolean {
        return declarations.isEmpty()
    }

    /**
     * Reverse operation of taking key by literal value.
     */
    fun getSymbolFromLiteral(literal: String): String {
        for ((k, v) in declarations) {
            if (v == literal) {
                return k
            }
        }
        // symbol not found. Create a new label.
        return addDeclaration(literal)
    }

    fun getInstructions() : List<WInstruction> {
        return listOf(
            Section(".data"),
            BlankLine()
        )
            .plus(declarations.map { declarationToInstruction(it) }.flatten())
            .plus(BlankLine())
    }

    private fun declarationToInstruction(entry: Map.Entry<String, String>): List<WInstruction> {
        return declarationToInstruction(entry.key, entry.value)
    }

    private fun declarationToInstruction(name: String, value: String) : List<WInstruction> {
        return listOf(
            Label(name),
            SubSection(".word", "${escapeStringSize(value)}"),
            SubSection(".ascii", "$THREE_SPACES\"$value\""),
        )
    }

    companion object {

        private const val THREE_SPACES = "   "

        private fun escapeStringSize(value: String): Int {
            var size = value.length
            if (value.contains("\\")) {
                var i = 0
                while (i < value.length) {
                    if (value[i] == '\\' && i < value.length - 1) {
                        size--
                        if (value[i + 1] == '\\') {
                            i++
                        }
                    }
                    i++
                }
            }
            return size
        }
    }
}