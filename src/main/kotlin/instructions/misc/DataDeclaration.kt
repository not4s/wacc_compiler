package instructions.misc

import instructions.WInstruction

class DataDeclaration {

    var declarations = mutableMapOf<String, String>()

    fun addDeclaration(name: String, value: String) {
        declarations[name] = value
    }

    fun getInstructions() : List<WInstruction> {
        return declarations.map { declarationToInstruction(it) }.flatten()
    }

    private fun declarationToInstruction(entry: Map.Entry<String, String>): List<WInstruction> {
        return declarationToInstruction(entry.key, entry.value)
    }

    private fun declarationToInstruction(name: String, value: String) : List<WInstruction> {
        return listOf(
            Label(name),
            SubSection(".word", "${value.length}"),
            SubSection(".ascii", "\"$value\""))
    }
}