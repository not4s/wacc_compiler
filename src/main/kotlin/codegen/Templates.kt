package codegen

import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*

const val P_PRINT_STRING = "p_print_string"
const val P_PRINT_LN = "p_print_ln"
const val PRINTF = "printf"
const val FFLUSH = "fflush"
const val PUTS = "puts"

const val NULL_CHAR = "\\0"
val ESCAPES: List<String> = listOf(NULL_CHAR, "\\b", "\\t", "\\n", "\\f", "\\r", "\\\"", "\\'", "\\\\")

const val NULL_TERMINAL_STRING = "%.*s$NULL_CHAR"

const val WORD_SIZE = 4

fun pPrintString(data: DataDeclaration): List<WInstruction> {
    return listOf(
        Label(P_PRINT_STRING),
        PUSH(Register.linkRegister()),
        LDR(Register(1), ImmediateOffset(Register.resultRegister())),
        ADD(Register(2), Register.resultRegister(), Immediate(WORD_SIZE)),
        LDR(Register.resultRegister(), LabelReference(NULL_TERMINAL_STRING, data)),
        ADD(Register.resultRegister(), Register.resultRegister(), Immediate(WORD_SIZE)),
        B(PRINTF, false, B.Condition.L),
        MOV(Register.resultRegister(), Immediate(0)),
        B(FFLUSH, false, B.Condition.L),
        POP(Register.programCounter())
    )
}

fun pPrintLn(data: DataDeclaration): List<WInstruction> {
    return listOf(
        Label(P_PRINT_LN),
        PUSH(Register.linkRegister()),
        LDR(Register.resultRegister(), LabelReference(NULL_CHAR, data)),
        ADD(Register.resultRegister(), Register.resultRegister(), Immediate(WORD_SIZE)),
        B(PUTS, false, B.Condition.L),
        MOV(Register.resultRegister(), Immediate(0)),
        B(FFLUSH, false, B.Condition.L),
        POP(Register.programCounter())
    )
}
