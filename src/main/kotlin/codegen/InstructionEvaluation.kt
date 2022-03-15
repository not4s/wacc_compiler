package codegen

import kotlin.collections.List
import instructions.WInstruction
import instructions.operations.LDR
import instructions.operations.STR
import instructions.operations.MOV
import instructions.operations.ADD
import instructions.misc.Register
import instructions.misc.ImmediateOffset
import instructions.misc.ShiftedRegister
import instructions.misc.Immediate

class InstructionEvaluation {

    companion object {

        // Peephole Optimisation
        fun evaluateInstructions(instructions: List<WInstruction>) : List<WInstruction> {
            // fetch the first instruction as prev
            var prev = instructions.first()
            var evaluated = listOf<WInstruction>(prev)
            
            // iterate over the instructions to remove redundant instructions
            for(instr in instructions) {
                // remove duplicate instructions
                if(instr == prev) continue
                val instr_list = instr.toString().replace(",", "").split(" ")
                val prev_list = prev.toString().replace(",", "").split(" ")
                when(instr) {    
                    is LDR -> {
                        // the instruction stores the content of a register
                        // but fetches the stored content back into the same register
                        if(prev is STR) {
                            val same_dst = instr_list[1] == prev_list[1]
                            var same_src = instr_list[2] == prev_list[2]
                            val same_flags = instr.isSignedByte == prev.isSignedByte

                            if(prev_list.size == 4) {
                                same_src = same_src && (instr_list[3] == prev_list[3])
                            }
                            if(same_dst && same_src && same_flags) continue
                        }
                    }
                    
                    is MOV -> {
                        // if MOV r4, r0; MOV r0, r4 the latter instruction is deleted
                        if(prev is MOV && prev_list.size == 3 
                                && prev_list[1] == instr_list[1]
                                && prev_list[2] == instr_list[2]) continue

                        // if two MOV instructions come sequentially change one to an ADD instruction
                        // Using the adder and shifter in parallel resultins in higher speed
                        if(prev is MOV && instr.op2 is Register) {
                                evaluated += ADD(instr.rDest, instr.op2, Immediate(0))
                                continue
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