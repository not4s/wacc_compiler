package codegen

import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*

const val P_PRINT_STRING = "p_print_string"
const val P_PRINT_LN = "p_print_ln"
const val P_PRINT_BOOL = "p_print_bool"
const val P_PRINT_INT = "p_print_int"
const val PRINTF = "printf"
const val FFLUSH = "fflush"
const val PUTCHAR = "putchar"
const val PUTS = "puts"

const val NULL_CHAR = "\\0"
const val NULL_TERMINAL_STRING = "%.*s$NULL_CHAR"
const val NULL_TERMINAL_INT = "%d$NULL_CHAR"
const val LITERAL_TRUE = "true$NULL_CHAR"
const val LITERAL_FALSE = "false$NULL_CHAR"

const val THROW_RUNTIME_ERROR = "p_throw_runtime_error"
const val THROW_OVERFLOW_ERROR = "p_throw_overflow_error"
const val OVERFLOW_ERROR_MESSAGE =
    "OverflowError: the result is too small/large to store in a 4-byte signed-integer.\\n\\0"
const val EXIT = "exit"

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
        LDR(
            Register.resultRegister(),
            LabelReference(LITERAL_TRUE, data),
            conditionCode = ConditionCode.NE
        ),
        LDR(
            Register.resultRegister(),
            LabelReference(LITERAL_FALSE, data),
            conditionCode = ConditionCode.EQ
        ),
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

fun pThrowRuntimeError(data: DataDeclaration, functionPool: FunctionPool): List<WInstruction> {
    var instructions: List<WInstruction> = listOf()
    if (!functionPool.containsFunc(THROW_RUNTIME_ERROR)) {
        instructions = listOf(
            Label(THROW_RUNTIME_ERROR),
            B(P_PRINT_STRING),
            MOV(Register.resultRegister(), Immediate(-1)),
            B(EXIT)
        )
        if (!functionPool.containsFunc(P_PRINT_STRING)) {
            instructions.plus(pPrintString(data))
        }
    }
    return instructions
}

fun pThrowOverflowError(data: DataDeclaration, functionPool: FunctionPool): List<WInstruction> {
    var instructions: List<WInstruction> = listOf()
    if (!functionPool.containsFunc(THROW_OVERFLOW_ERROR)) {
        instructions = listOf(
            LDR(Register.resultRegister(), LabelReference(OVERFLOW_ERROR_MESSAGE, data))
        )
        if (!functionPool.containsFunc(THROW_RUNTIME_ERROR)) {
            instructions.plus(pThrowRuntimeError(data))
        }
    }
    return instructions
}