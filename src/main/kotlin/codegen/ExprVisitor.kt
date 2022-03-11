package codegen

import ast.*
import instructions.WInstruction
import instructions.misc.DataDeclaration
import instructions.misc.Immediate
import instructions.misc.Register
import instructions.misc.ShiftedRegister
import instructions.operations.*

class ExprVisitor(
    val data: DataDeclaration,
    private val registerProvider: RegisterProvider,
    private val funcPool: FunctionPool
) : ASTVisitor<Expr> {
    override fun visit(ctx: Expr): List<WInstruction> {
        return when (ctx) {
            is Literal -> {
                RHSVisitor(data, registerProvider, funcPool).visit(ctx)
            }

            is IdentifierGet -> {
                ctx.st.asmGet(ctx.identifier, Register.R0, data)
            }

            is ArrayElement -> {
                pCheckArrayBounds(data, funcPool)
                ctx.st.asmGet(
                    ctx.identifier,
                    ctx.indices,
                    Register.R0,
                    data,
                    registerProvider,
                    funcPool
                )
            }

            is UnaryOperation -> {
                // Evaluate expr, result will be in R0
                val instr = visit(ctx.operand)

                when (ctx.operation) {
                    UnOperator.SUB -> {
                        pThrowOverflowError(data, funcPool)
                        return instr.plus(
                            listOf(
                                RSB(Register.R0, Register.R0),
                                B("p_throw_overflow_error", cond = B.Condition.VS),
                            )
                        )
                    }
                    UnOperator.NOT -> {
                        return instr.plus(
                            listOf(
                                EOR(Register.R0, Register.R0, Immediate(1))
                            )
                        )
                    }
                    UnOperator.CHR, UnOperator.ORD -> {
                        return instr // char = int lol
                    }
                    UnOperator.LEN -> {
                        return instr.plus(
                            listOf(
                                LDR(Register.R0, Register.R0)
                            )
                        )
                    }
                }
            }

            is BinaryOperation -> {

                val reg1 = Register.R0
                val reg2 = Register.R1

                val instr =
                    // Evaluate right, result will be in R0. Push this to stack.
                    visit(ctx.right)
                        .plus(PUSH(Register.R0, data))
                        // Eval left. Result is stored in R0.
                        .plus(visit(ctx.left))
                        // Pop the right result to R1.
                        .plus(POP(reg2, data))

                return when (ctx.op) {

                    BinOperator.MUL -> {
                        pThrowOverflowError(data, funcPool)
                        instr.plus(
                            listOf(
                                SMULL(reg1, reg2, reg1, reg2),
                                CMP(reg2, ShiftedRegister(reg1, 31)),
                                B("p_throw_overflow_error", cond=B.Condition.NE)
                            )
                        )
                    }

                    BinOperator.DIV -> {
                        pCheckDivideByZero(data, funcPool)
                        instr.plus(
                            listOf(
                                B("p_check_divide_by_zero"),
                                B("__aeabi_idiv")
                            )
                        )
                    }

                    BinOperator.MOD -> {
                        pCheckDivideByZero(data, funcPool)
                        instr.plus(
                            listOf(
                                MOV(Register.R0, reg1),
                                MOV(Register.R1, reg2),
                                B("p_check_divide_by_zero"),
                                B("__aeabi_idivmod"),
                                MOV(reg1, Register.R1)
                            )
                        )
                    }

                    BinOperator.ADD -> {
                        pThrowOverflowError(data, funcPool)
                        val addInstr = ADD(reg1, reg1, reg2)
                        addInstr.flagSet = true
                        instr.plus(
                            listOf(
                                addInstr,
                                B("p_throw_overflow_error", cond = B.Condition.VS),
                            )
                        )
                    }

                    BinOperator.SUB -> {
                        pThrowOverflowError(data, funcPool)
                        val subInstr = SUB(reg1, reg1, reg2)
                        subInstr.flagSet = true
                        instr.plus(
                            listOf(
                                subInstr,
                                B("p_throw_overflow_error", cond = B.Condition.VS),
                            )
                        )
                    }

                    BinOperator.GT -> instr.plus(
                        listOf(
                            CMP(reg1, reg2),
                            MOV(reg1, Immediate(1), MOV.Condition.GT),
                            MOV(reg1, Immediate(0), MOV.Condition.LE)
                        )
                    )

                    BinOperator.GEQ -> instr.plus(
                        listOf(
                            CMP(reg1, reg2),
                            MOV(reg1, Immediate(1), MOV.Condition.GE),
                            MOV(reg1, Immediate(0), MOV.Condition.LT)
                        )
                    )

                    BinOperator.LT -> instr.plus(
                        listOf(
                            CMP(reg1, reg2),
                            MOV(reg1, Immediate(1), MOV.Condition.LT),
                            MOV(reg1, Immediate(0), MOV.Condition.GE)
                        )
                    )

                    BinOperator.LEQ -> instr.plus(
                        listOf(
                            CMP(reg1, reg2),
                            MOV(reg1, Immediate(1), MOV.Condition.LE),
                            MOV(reg1, Immediate(0), MOV.Condition.GT)
                        )
                    )

                    BinOperator.EQ -> instr.plus(
                        listOf(
                            CMP(reg1, reg2),
                            MOV(reg1, Immediate(1), MOV.Condition.EQ),
                            MOV(reg1, Immediate(0), MOV.Condition.NE)
                        )
                    )

                    BinOperator.NEQ -> instr.plus(
                        listOf(
                            CMP(reg1, reg2),
                            MOV(reg1, Immediate(1), MOV.Condition.NE),
                            MOV(reg1, Immediate(0), MOV.Condition.EQ)
                        )
                    )

                    BinOperator.AND -> instr.plus(AND(reg1, reg1, reg2))

                    BinOperator.OR -> instr.plus(ORR(reg1, reg1, reg2))
                }
            }

            is PairLiteral -> {
                // Delegate to RHS visitor of literals
                RHSVisitor(data, registerProvider, funcPool).visit(ctx)
            }
        }

    }
}
