package codegen

import ast.*
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*

class ExprVisitor(
    val data: DataDeclaration,
    private val registerProvider: RegisterProvider,
) : ASTVisitor<Expr> {

    var resultStored: Operand2? = null

    override fun visit(ctx: Expr): List<WInstruction> {
        return when (ctx) {
            is Literal -> {
                // Delegate to RHS visitor of literals
                resultStored = Register.resultRegister()
                RHSVisitor(data, registerProvider).visit(ctx)
            }

            is IdentifierGet -> {
                resultStored = Register.resultRegister()
                ctx.st.asmGet(ctx.identifier, Register.resultRegister())
            }
            is BinaryOperation -> {
                var reg1 = registerProvider.get()
                var reg2 = registerProvider.get()

                val instr =
                    // Evaluate left, result will be in R0.
                    visit(ctx.left)
                        .plus(MOV(reg1, Register("r0")))
                        // Eval right
                        .plus(visit(ctx.right))
                        .plus(MOV(reg2, Register("r0")))
                        // Move to scratch registers
                        .plus(MOV(Register("r0"), reg1))
                        .plus(MOV(Register("r1"), reg2))
                registerProvider.ret()
                registerProvider.ret()
                resultStored = Register.resultRegister()

                reg1 = Register("r0")
                reg2 = Register("r1")
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
