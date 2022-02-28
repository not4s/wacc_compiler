package codegen

import instructions.WInstruction
import instructions.aux.BlankLine
import instructions.aux.Label
import instructions.aux.Section
import instructions.aux.SubSection

class WInstrToString {

    companion object {
        fun translateInstructions(instructions: List<WInstruction>) {
            println(instructions.joinToString("\n") {convertInstruction(it)} + "\n\t")
        }

        private fun convertInstruction(it: WInstruction): String {
            return "\t" + when(it) {
                is SubSection -> "\t$it"
                is Label, is Section, is BlankLine -> "$it"
                else -> "\t$it"
            }
        }
    }

}