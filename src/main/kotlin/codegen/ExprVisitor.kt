package codegen

import ast.*
import instructions.WInstruction
import instructions.misc.Immediate
import instructions.misc.Operand2
import instructions.misc.Register
import instructions.misc.shiftedRegister
import instructions.operations.*
import waccType.WInt

class ExprVisitor(
    private val registerProvider: RegisterProvider
) : ASTVisitor<Expr> {

    var resultStored: Operand2? = null

    override fun visit(ctx: Expr): List<WInstruction> {
        return when (ctx) {
            is Literal -> when (ctx.type) {
                is WInt -> {
                    val exitCode = ctx.type.value ?: throw Exception("Exit code not found")
                    resultStored = Immediate(exitCode)
                    listOf()
                }
                else -> throw Exception("Somehow not an int exit")
            }
            is IdentifierGet -> {
                val reg = registerProvider.get()
                resultStored = reg
                ctx.st.asmGet(ctx.identifier, reg)
            }
            is BinaryOperation -> {
                val instr = mutableListOf<WInstruction>()
                val reg = registerProvider.get()
                val nextReg = Register("r${(reg.rName.substring(1).toInt() + 1)}")

                val reg1 = reg
                val reg2 = nextReg

                when (ctx.op) {

                    BinOperator.MUL ->
                        return instr.plus(
                            listOf(
                                SMULL(reg1, reg2, reg1, reg2),
                                CMP(reg2, shiftedRegister(reg1, 31)),
                                B("p_throw_overflow_error", true, B.Condition.NE)
                            )
                        )

                    BinOperator.DIV ->
                        return instr.plus(
                            listOf(
                                MOV(Register("r0"), reg1),
                                MOV(Register("r1"), reg2),
                                B("p_check_divide_by_zero", true),
                                B("__aeabi_idiv", true),
                                MOV(reg1, Register("r0"))
                            )
                        )

                    BinOperator.MOD ->
                        return instr.plus(
                            listOf(
                                MOV(Register("r0"), reg1),
                                MOV(Register("r1"), reg2),
                                B("p_check_divide_by_zero", true),
                                B("__aeabi_idivmod", true),
                                MOV(reg1, Register("r1"))
                            )
                        )

                    BinOperator.ADD -> {
                        val addInstr = ADD(reg1, reg1, reg2)
                        addInstr.flagSet = true
                        return instr.plus(
                            listOf(
                                addInstr,
                                B("p_throw_overflow_error", true),
                            )
                        )
                    }

                    BinOperator.SUB -> {
                        val subInstr = SUB(reg1, reg1, reg2)
                        subInstr.flagSet = true
                        return instr.plus(
                            listOf(
                                subInstr,
                                B("p_throw_overflow_error", true),
                            )
                        )
                    }

                    BinOperator.GT ->
                        return instr.plus(
                            listOf(
                                CMP(reg1, reg2),
                                MOV(reg1, Immediate(1), MOV.Condition.GT),
                                MOV(reg1, Immediate(0), MOV.Condition.LE)
                            )
                        )

                    BinOperator.GEQ ->
                        return instr.plus(
                            listOf(
                                CMP(reg1, reg2),
                                MOV(reg1, Immediate(1), MOV.Condition.GE),
                                MOV(reg1, Immediate(0), MOV.Condition.LT)
                            )
                        )

                    BinOperator.LT ->
                        return instr.plus(
                            listOf(
                                CMP(reg1, reg2),
                                MOV(reg1, Immediate(1), MOV.Condition.LT),
                                MOV(reg1, Immediate(0), MOV.Condition.GE)
                            )
                        )

                    BinOperator.LEQ ->
                        return instr.plus(
                            listOf(
                                CMP(reg1, reg2),
                                MOV(reg1, Immediate(1), MOV.Condition.LE),
                                MOV(reg1, Immediate(0), MOV.Condition.GT)
                            )
                        )

                    BinOperator.EQ ->
                        return instr.plus(
                            listOf(
                                CMP(reg1, reg2),
                                MOV(reg1, Immediate(1), MOV.Condition.EQ),
                                MOV(reg1, Immediate(0), MOV.Condition.NE)
                            )
                        )

                    BinOperator.NEQ ->
                        return instr.plus(
                            listOf(
                                CMP(reg1, reg2),
                                MOV(reg1, Immediate(1), MOV.Condition.NE),
                                MOV(reg1, Immediate(0), MOV.Condition.EQ)
                            )
                        )

                    BinOperator.AND ->
                        return instr.plus(AND(reg1, reg1, reg2))

                    BinOperator.OR ->
                        return instr.plus(ORR(reg1, reg1, reg2))

                    else -> TODO()
                }
            }
            else -> TODO()
        }

    }
}
