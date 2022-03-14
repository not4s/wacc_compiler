package codegen

import kotlin.collections.List
import instructions.WInstruction
import instructions.operations.LDR
import instructions.operations.STR
import instructions.operations.MOV

class InstructionEvaluation {

    companion object {

        fun evaluateInstructions(instructions: List<WInstruction>) : List<WInstruction> {
            // fetch the first instruction as prev
            var prev = instructions.first()
            var evaluated = listOf<WInstruction>(prev)
            
            // iterate over the instructions to remove redundant instructions
            for(instr in instructions) {
                // remove duplicate instructions
                if(instr == prev) continue

                when(instr) {
                    is LDR
                        -> {
                            // the instruction stores the content of a register
                            // but fetches the stored content back into the same register
                            if(prev is STR) {
                                val instr_list = instr.toString().split(" ")
                                val prev_list = prev.toString().split(" ")
                                val same_dst = instr_list[1] == prev_list[1]
                                var same_src = instr_list[2] == prev_list[2]
                                val same_flags = instr.isSignedByte == prev.isSignedByte

                                if(prev_list.size == 4) {
                                    same_src = same_src && (instr_list[3] == prev_list[3])
                                }
                                if(same_dst && same_src && same_flags) continue
                            }
                        }
                }
                evaluated += instr
                prev = instr
            }
            return evaluated
        }
    }
}