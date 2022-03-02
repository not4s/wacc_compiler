package codegen

import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*

const val P_PRINT_STRING = "p_print_string"
const val P_PRINT_LN = "p_print_ln"
const val P_PRINT_BOOL = "p_print_bool"
const val P_PRINT_INT = "p_print_int"
const val P_READ_INT = "p_read_int"
const val P_READ_CHAR = "p_read_char"
const val PRINTF = "printf"
const val SCANF = "scanf"
const val FFLUSH = "fflush"
const val PUTCHAR = "putchar"
const val MALLOC = "malloc"
const val PUTS = "puts"

const val NULL_CHAR = "\\0"
const val NULL_TERMINAL_STRING = "%.*s$NULL_CHAR"
const val NULL_TERMINAL_INT = "%d$NULL_CHAR"
const val NULL_TERMINAL_CHAR = "%c$NULL_CHAR"
const val LITERAL_TRUE = "true$NULL_CHAR"
const val LITERAL_FALSE = "false$NULL_CHAR"

const val WORD_SIZE = 4

val printFunEnd: List<WInstruction> = listOf(
    MOV(Register.resultRegister(), Immediate(0)),
    B(FFLUSH, link = true),
    POP(Register.programCounter())
)

fun pPrintString(data: DataDeclaration): List<WInstruction> {
    return listOf(
        Label(P_PRINT_STRING),
        PUSH(Register.linkRegister()),
        LDR(Register(1), ImmediateOffset(Register.resultRegister())),
        ADD(Register(2), Register.resultRegister(), Immediate(WORD_SIZE)),
        LDR(Register.resultRegister(), LabelReference(NULL_TERMINAL_STRING, data)),
        ADD(Register.resultRegister(), Register.resultRegister(), Immediate(WORD_SIZE)),
        B(PRINTF, link = true)
    ).plus(printFunEnd)
}

fun pPrintLn(data: DataDeclaration): List<WInstruction> {
    return listOf(
        Label(P_PRINT_LN),
        PUSH(Register.linkRegister()),
        LDR(Register.resultRegister(), LabelReference(NULL_CHAR, data)),
        ADD(Register.resultRegister(), Register.resultRegister(), Immediate(WORD_SIZE)),
        B(PUTS, link = true)
    ).plus(printFunEnd)
}

fun pPrintBool(data: DataDeclaration): List<WInstruction> {
    return listOf(
        Label(P_PRINT_BOOL),
        PUSH(Register.linkRegister()),
        CMP(Register.resultRegister(), Immediate(0)),
        LDR(Register.resultRegister(), LabelReference(LITERAL_TRUE, data), conditionCode = ConditionCode.NE),
        LDR(Register.resultRegister(), LabelReference(LITERAL_FALSE, data), conditionCode = ConditionCode.EQ),
        ADD(Register.resultRegister(), Register.resultRegister(), Immediate(WORD_SIZE)),
        B(PRINTF, link = true)
    ).plus(printFunEnd)
}

fun pPrintInt(data: DataDeclaration): List<WInstruction> {
    return listOf(
        Label(P_PRINT_INT),
        PUSH(Register.linkRegister()),
        MOV(Register(1), Register.resultRegister()),
        LDR(Register.resultRegister(), LabelReference(NULL_TERMINAL_INT, data)),
        ADD(Register.resultRegister(), Register.resultRegister(), Immediate(WORD_SIZE)),
        B(PRINTF, link = true)
    ).plus(printFunEnd)
}

fun pReadInt(data: DataDeclaration): List<WInstruction> {
    return listOf(
        Label(P_READ_INT),
        PUSH(Register.linkRegister()),
        MOV(Register(1), Register.resultRegister()),
        LDR(Register.resultRegister(), LabelReference(NULL_TERMINAL_INT, data)),
        ADD(Register.resultRegister(), Register.resultRegister(), Immediate(WORD_SIZE)),
        B(SCANF, link = true),
        POP(Register.programCounter())
    )
}

fun pReadChar(data: DataDeclaration): List<WInstruction> {
    return listOf(
        Label(P_READ_CHAR),
        PUSH(Register.linkRegister()),
        MOV(Register(1), Register.resultRegister()),
        LDR(Register.resultRegister(), LabelReference(NULL_TERMINAL_CHAR, data)),
        ADD(Register.resultRegister(), Register.resultRegister(), Immediate(WORD_SIZE)),
        B(SCANF, link = true),
        POP(Register.programCounter())
    )
}
