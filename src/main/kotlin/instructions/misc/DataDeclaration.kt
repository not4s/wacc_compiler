package instructions.misc

import instructions.WInstruction

class DataDeclaration {

    private var noNameMsgCounter: Int = 0
    private val declarations = mutableMapOf<String, String>()

    fun addDeclaration(name: String, value: String) {
        declarations[name] = value
    }

    fun addDeclaration(literal: String) {
        declarations["msg_${noNameMsgCounter++}"] = literal
    }

    fun isEmpty(): Boolean {
        return declarations.isEmpty()
    }

    /**
     * Reverse operation of taking key by literal value.
     * TODO: Maybe reverse the map?
     */
    fun getSymbolFromLiteral(literal: String): String {
        for ((k, v) in declarations) {
            if (v == literal) {
                return k
            }
        }
        throw Exception("Could not find symbol for literal $literal in map: $declarations")
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