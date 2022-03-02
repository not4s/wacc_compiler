package codegen

import ast.*
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*

class ExprVisitor(
    val data: DataDeclaration,
    private val registerProvider: RegisterProvider,
    private val funcPool: FunctionPool
) : ASTVisitor<Expr> {

    var resultStored: Operand2? = null

    override fun visit(ctx: Expr): List<WInstruction> {
        return when (ctx) {
            is Literal -> {
                // Delegate to RHS visitor of literals
                resultStored = Register.resultRegister()
                RHSVisitor(data, registerProvider, funcPool).visit(ctx)
            }

            is IdentifierGet -> {
                resultStored = Register.resultRegister()
                ctx.st.asmGet(ctx.identifier, Register.resultRegister(), data)
            }
            is UnaryOperation -> {
                val instr =
                    // Evaluate expr, result will be in R0
                    visit(ctx.operand)

                resultStored = Register.resultRegister()

                when (ctx.operation) {

                    UnOperator.SUB -> {
                        pThrowOverflowError(data, funcPool)
                        return instr.plus(
                            listOf(
                                RSB(Register.resultRegister(), Register.resultRegister()),
                                B("p_throw_overflow_error", true, cond = B.Condition.VS),
                            )
                        )
                    }
                    UnOperator.NOT -> {
                        return instr.plus(
                            listOf(
                                EOR(Register.resultRegister(), Register.resultRegister(), Immediate(1))
                            )
                        )
                    }
                    UnOperator.CHR, UnOperator.ORD -> {
                        return instr // char = int lol
                    }

                    else -> TODO()
                }
            }
            is BinaryOperation -> {

                val reg1 = Register("r0")
                val reg2 = Register("r1")

                val instr =
                    // Evaluate right, result will be in R0. Push this to stack.
                    visit(ctx.right)
                        .plus(PUSH(Register.resultRegister(), data))
                        // Eval left. Result is stored in R0.
                        .plus(visit(ctx.left))
                        // Pop the right result to R1.
                        .plus(POP(reg2, data))

                resultStored = Register.resultRegister()

                when (ctx.op) {

                    BinOperator.MUL -> {
                        pThrowOverflowError(data, funcPool)
                        return instr.plus(
                            listOf(
                                SMULL(reg1, reg2, reg1, reg2),
                                CMP(reg2, ShiftedRegister(reg1, 31)),
                                B("p_throw_overflow_error", true, B.Condition.NE)
                            )
                        )
                    }

                    BinOperator.DIV -> {
                        pCheckDivideByZero(data, funcPool)
                        return instr.plus(
                            listOf(
                                B("p_check_divide_by_zero", true),
                                B("__aeabi_idiv", true),
                            )
                        )
                    }

                    BinOperator.MOD -> {
                        pCheckDivideByZero(data, funcPool)
                        return instr.plus(
                            listOf(
                                MOV(Register("r0"), reg1),
                                MOV(Register("r1"), reg2),
                                B("p_check_divide_by_zero", true),
                                B("__aeabi_idivmod", true),
                                MOV(reg1, Register("r1"))
                            )
                        )
                    }

                    BinOperator.ADD -> {
                        pThrowOverflowError(data, funcPool)
                        val addInstr = ADD(reg1, reg1, reg2)
                        addInstr.flagSet = true
                        return instr.plus(
                            listOf(
                                addInstr,
                                B("p_throw_overflow_error", true, cond = B.Condition.VS),
                            )
                        )
                    }

                    BinOperator.SUB -> {
                        pThrowOverflowError(data, funcPool)
                        val subInstr = SUB(reg1, reg1, reg2)
                        subInstr.flagSet = true
                        return instr.plus(
                            listOf(
                                subInstr,
                                B("p_throw_overflow_error", true, cond = B.Condition.VS),
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
