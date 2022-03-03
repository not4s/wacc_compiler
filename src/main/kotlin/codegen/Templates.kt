package codegen

import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*

const val P_PRINT_REFERENCE = "p_print_reference"
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
const val NULL_TERMINAL_CHAR = " %c$NULL_CHAR"
const val NULL_TERMINAL_REFERENCE = "%p$NULL_CHAR"
const val LITERAL_TRUE = "true$NULL_CHAR"
const val LITERAL_FALSE = "false$NULL_CHAR"

const val THROW_RUNTIME_ERROR = "p_throw_runtime_error"
const val THROW_OVERFLOW_ERROR = "p_throw_overflow_error"
const val CHECK_DIVIDE_BY_ZERO = "p_check_divide_by_zero"
const val CHECK_NULL_POINTER = "p_check_null_pointer"
const val OVERFLOW_ERROR_MESSAGE =
    "OverflowError: the result is too small/large to store in a 4-byte signed-integer.\\n\\0"
const val CHECK_ARRAY_BOUNDS = "p_check_array_bounds"
const val ARRAY_BOUNDS_ERROR_MESSAGE = "ArrayIndexOutOfBoundsError: index too large\\n\\0"
const val ARRAY_NEGATIVE_ERROR_MESSAGE = "ArrayIndexOutOfBoundsError: negative index\\n\\0"
const val DIVIDE_BY_ZERO_MESSAGE = "DivideByZeroError: divide or modulo by zero\\n\\0"
const val NULL_POINTER_MESSAGE = "NullReferenceError: dereference a null reference\\n\\0"
const val EXIT = "exit"

const val WORD_SIZE = 4
const val PAIR_SIZE = 4
const val INT_SIZE = 4
const val STR_SIZE = 4
const val BOOL_SIZE = 1
const val CHAR_SIZE = 1

val printFunEnd: List<WInstruction> = listOf(
    MOV(Register.resultRegister(), Immediate(0)),
    B(FFLUSH, link = true),
    POP(Register.programCounter())
)

fun funcLabel(funcName: String): String {
    return "f_$funcName"
}

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

fun pPrintReference(data: DataDeclaration): List<WInstruction> {
    return listOf(
        Label(P_PRINT_REFERENCE),
        PUSH(Register.linkRegister()),
        MOV(Register(1), Register.resultRegister()),
        LDR(Register.resultRegister(), LabelReference(NULL_TERMINAL_REFERENCE, data)),
        ADD(Register.resultRegister(), Register.resultRegister(), Immediate(PAIR_SIZE)),
        B(PRINTF, true),
        MOV(Register.resultRegister(), Immediate(0)),
        B(FFLUSH, true),
        POP(Register.programCounter())
    )
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

fun pThrowRuntimeError(data: DataDeclaration, functionPool: FunctionPool) {
    functionPool.add(
        listOf(
            Label(THROW_RUNTIME_ERROR),
            B(P_PRINT_STRING, link = true),
            MOV(Register.resultRegister(), Immediate(-1)),
            B(EXIT, link = true)
        )
    )
    // add dependencies if not added yet
    functionPool.add(pPrintString(data))
}

fun pThrowOverflowError(data: DataDeclaration, functionPool: FunctionPool) {
    functionPool.add(
        listOf(
            Label(THROW_OVERFLOW_ERROR),
            LDR(Register.resultRegister(), LabelReference(OVERFLOW_ERROR_MESSAGE, data)),
            B(THROW_RUNTIME_ERROR, link = true)
        )
    )
    // add dependencies if not added yet
    pThrowRuntimeError(data, functionPool)
}

fun pCheckDivideByZero(data: DataDeclaration, functionPool: FunctionPool) {
    functionPool.add(
        listOf(
            Label(CHECK_DIVIDE_BY_ZERO),
            PUSH(Register.linkRegister()),
            CMP(Register("r1"), Immediate(0)),
            LDR(
                Register.resultRegister(),
                LabelReference(DIVIDE_BY_ZERO_MESSAGE, data),
                conditionCode = ConditionCode.EQ
            ),
            B(THROW_RUNTIME_ERROR, link = true, cond = B.Condition.EQ),
            POP(Register.programCounter())
        )
    )
    // add dependencies if not added yet
    pThrowRuntimeError(data, functionPool)
}

fun pCheckArrayBounds(data: DataDeclaration, functionPool: FunctionPool) {
    functionPool.add(
        listOf(
            Label(CHECK_ARRAY_BOUNDS),
            PUSH(Register.linkRegister()),
            CMP(Register.resultRegister(), Immediate(0)),
            LDR(
                Register.resultRegister(),
                LabelReference(ARRAY_NEGATIVE_ERROR_MESSAGE, data),
                conditionCode = ConditionCode.LT
            ),
            B(THROW_RUNTIME_ERROR, link = true, cond = B.Condition.LT),
            LDR(Register("r1"), ImmediateOffset(Register("r4"))),
            CMP(Register.resultRegister(), Register("r1")),
            LDR(
                Register.resultRegister(),
                LabelReference(ARRAY_BOUNDS_ERROR_MESSAGE, data),
                conditionCode = ConditionCode.CS
            ),
            B(THROW_RUNTIME_ERROR, link = true, cond = B.Condition.CS),
            POP(Register.programCounter())
        )
    )
    // add dependencies if not added yet
    pThrowRuntimeError(data, functionPool)
}

fun pCheckNullPointer(data: DataDeclaration, functionPool: FunctionPool) {
    functionPool.add(
        listOf(
            Label(CHECK_NULL_POINTER),
            PUSH(Register.linkRegister()),
            CMP(Register("r0"), Immediate(0)),
            LDR(
                Register.resultRegister(),
                LabelReference(NULL_POINTER_MESSAGE, data),
                false,
                ConditionCode.EQ
            ),
            B(THROW_RUNTIME_ERROR, true, B.Condition.EQ),
            POP(Register.programCounter())
        )
    )
    // add dependencies if not added yet
    pThrowRuntimeError(data, functionPool)
}

fun pPrintReference(data: DataDeclaration, functionPool: FunctionPool) {
    functionPool.add(
        listOf(
            Label(P_PRINT_REFERENCE),
            PUSH(Register.linkRegister()),
            MOV(Register("r1"), Register.resultRegister()),
            LDR(Register.resultRegister(), LabelReference(NULL_TERMINAL_REFERENCE, data)),
            ADD(Register.resultRegister(), Register.resultRegister(), Immediate(4)),
            B(PRINTF, link = true),
            MOV(Register.resultRegister(), Immediate(0)),
            B(FFLUSH, link = true),
            POP(Register.programCounter())
        )
    )
    // add dependencies if not added yet
    functionPool.add(pPrintLn(data))
}
