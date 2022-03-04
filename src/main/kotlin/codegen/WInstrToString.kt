package codegen

import instructions.WInstruction
import instructions.misc.BlankLine
import instructions.misc.Label
import instructions.misc.Section
import instructions.misc.SubSection

class WInstrToString {

    companion object {
        fun translateInstructions(instructions: List<WInstruction>) : String {
            return instructions.joinToString("\n") {convertInstruction(it)} + "\n\t"
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