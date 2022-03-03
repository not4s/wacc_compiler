package codegen

import ast.*
import ast.statement.*
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*
import utils.btoi
import waccType.*

class StatVisitor(
    val data: DataDeclaration,
    private val funcPool: FunctionPool,
) : ASTVisitor<Stat> {

    private val registerProvider = RegisterProvider()

    override fun visit(ctx: Stat): List<WInstruction> {
        return when (ctx) {
            is SkipStat -> listOf()
            is ExitStat -> visitExitStat(ctx)
            is Declaration -> visitDeclarationStat(ctx)
            is Assignment -> visitAssignStat(ctx)
            is JoinStat -> {
                if (ctx.first.st !== ctx.st) {
                    offsetStackBy(ctx.first.st.totalByteSize).plus(
                        visit(ctx.first)
                    ).plus(
                        unOffsetStackBy(ctx.first.st.totalByteSize)
                    )
                } else {
                    visit(ctx.first)
                }.plus(
                    if (ctx.second.st !== ctx.st) {
                        offsetStackBy(ctx.second.st.totalByteSize).plus(
                            visit(ctx.second)
                        ).plus(
                            unOffsetStackBy(ctx.second.st.totalByteSize)
                        )
                    } else {
                        visit(ctx.second)
                    }
                )
            }
            is PrintStat -> visitPrintStat(ctx)
            is ReadStat -> visitReadStat(ctx)
            is IfThenStat -> visitIfThenStat(ctx)
            is WhileStat -> visitWhileStat(ctx)
            else -> TODO("Not yet implemented")
        }
    }

    private fun visitWhileStat(ctx: WhileStat): List<WInstruction> {
        val whileStartLabel: String = funcPool.getAbstractLabel()
        val whileBodyLabel: String = funcPool.getAbstractLabel()

        val exprVisitor = ExprVisitor(data, registerProvider, funcPool)
        val condition = exprVisitor.visit(ctx.condition)

        val whileBody: List<WInstruction> = StatVisitor(data, funcPool).visit(ctx.doBlock)
        val jump = B(whileBodyLabel, cond = B.Condition.NE)

        return listOf(B(whileStartLabel))
            .plus(Label(whileBodyLabel))
            .plus(whileBody)
            .plus(Label(whileStartLabel))
            .plus(condition)
            .plus(CMP(Register.resultRegister(), Immediate(0)))
            .plus(jump)
    }

    private fun visitReadStat(ctx: ReadStat): List<WInstruction> {
        val output = mutableListOf<WInstruction>()
        val readFun: String
        when (ctx.lhs.type) {
            is WInt -> {
                readFun = P_READ_INT
                data.addDeclaration(NULL_TERMINAL_INT)
                funcPool.add(pReadInt(data))
                output.add(ADD(Register.resultRegister(), Register.stackPointer(), Immediate(0)))
            }
            is WChar -> {
                readFun = P_READ_CHAR
                data.addDeclaration(NULL_TERMINAL_CHAR)
                funcPool.add(pReadChar(data))
                output.add(ADD(Register.resultRegister(), Register.stackPointer(), Immediate(0)))
            }
            else -> throw Exception("Can only read chars or ints")
        }

        return output.plus(
            B(readFun, link = true)
        ).plus(
            when (ctx.lhs) {
                is IdentifierSet -> ctx.st.asmGet(ctx.lhs.identifier, Register.resultRegister(), data)
                else -> TODO()
            }
        )

    }

    private fun visitIfThenStat(ctx: IfThenStat): List<WInstruction> {
        // evaluate conditional
        val exprVisitor = ExprVisitor(data, registerProvider, funcPool)
        val condition = exprVisitor.visit(ctx.condition)

        val elseBranchLabel: String = funcPool.getAbstractLabel()
        val afterIfLabel: String = funcPool.getAbstractLabel()

        val thenCode: List<WInstruction> = offsetStackBy(ctx.thenStat.st.totalByteSize)
            .plus(StatVisitor(data, funcPool).visit(ctx.thenStat))
            .plus(unOffsetStackBy(ctx.thenStat.st.totalByteSize))
        val elseCode: List<WInstruction> = offsetStackBy(ctx.elseStat.st.totalByteSize)
            .plus(StatVisitor(data, funcPool).visit(ctx.elseStat))
            .plus(unOffsetStackBy(ctx.elseStat.st.totalByteSize))

        // compare with false, branch if equal (else branch)
        val jump = listOf(
            CMP(exprVisitor.resultStored!! as Register, Immediate(0)),
            B(elseBranchLabel, cond = B.Condition.EQ)
        )

        return condition
            .plus(jump)
            .plus(thenCode)
            .plus(B(afterIfLabel))
            .plus(Label(elseBranchLabel))
            .plus(elseCode)
            .plus(Label(afterIfLabel))
    }

    private fun visitExitStat(ctx: ExitStat): List<WInstruction> {
        val exprVisitor = ExprVisitor(data, registerProvider, funcPool)
        val evaluationCode = exprVisitor.visit(ctx.expr)
        return evaluationCode.plus(
            listOf(
                MOV(Register.resultRegister(), exprVisitor.resultStored!!),
                B("exit", link = true)
            )
        )
    }

    private fun visitPrintStat(ctx: PrintStat): List<WInstruction> {
        val literal: String
        val printFun: String
        val ldrDestReg = registerProvider.get()
        val firstArgInitInstruction: WInstruction
        var evalExprInstructions: List<WInstruction> = listOf()

        // Add general type things
        when (val type = ctx.expr.type) {
            is WStr -> {
                printFun = P_PRINT_STRING
                data.addDeclaration(NULL_TERMINAL_STRING)
                funcPool.add(pPrintString(data))
            }
            is WBool -> {
                printFun = P_PRINT_BOOL
                data.addDeclaration(LITERAL_TRUE)
                data.addDeclaration(LITERAL_FALSE)
                funcPool.add(pPrintBool(data))
            }
            is WInt -> {
                printFun = P_PRINT_INT
                data.addDeclaration(NULL_TERMINAL_INT)
                funcPool.add(pPrintInt(data))
            }
            is WChar -> {
                printFun = PUTCHAR
            }
            is WArray -> {
                if (type.elemType is WChar) {
                    printFun = P_PRINT_STRING
                    data.addDeclaration(NULL_TERMINAL_STRING)
                    funcPool.add(pPrintString(data))
                } else {
                    printFun = P_PRINT_REFERENCE
                    data.addDeclaration(NULL_TERMINAL_REFERENCE)
                    pPrintReference(data, funcPool)
                }
            }
            else -> TODO("Not yet implemented")
        }

        // Specific literal check
        if (ctx.expr is Literal) {
            when (ctx.expr.type) {
                is WStr -> {
                    literal = ctx.expr.type.value ?: throw Exception("Unspecified Literal string")
                    val reference = data.addDeclaration(literal)
                    firstArgInitInstruction = LDR(ldrDestReg, LabelReference(reference))
                }
                is WBool -> {
                    literal = ctx.expr.type.value.toString() + NULL_CHAR
                    firstArgInitInstruction =
                        MOV(ldrDestReg, Immediate(btoi(literal == LITERAL_TRUE)))
                }
                is WInt -> {
                    literal = ctx.expr.type.value.toString()
                    firstArgInitInstruction = MOV(ldrDestReg, Immediate(literal.toInt()))
                }
                is WChar -> {
                    firstArgInitInstruction = MOV(ldrDestReg, ImmediateChar(ctx.expr.type.value!!))
                }
                else -> TODO("Non-String literals are not supported. They will require things like %s %d etc")
            }
        } else {
            val exprVisitor = ExprVisitor(data, registerProvider, funcPool)
            evalExprInstructions = exprVisitor.visit(ctx.expr)
            firstArgInitInstruction = MOV(ldrDestReg, exprVisitor.resultStored!!)
        }

        if (ctx.newlineAfter) {
            data.addDeclaration(NULL_CHAR)
            funcPool.add(pPrintLn(data))
        }

        registerProvider.ret()
        return evalExprInstructions.plus(
            listOf(
                firstArgInitInstruction,
                MOV(Register.resultRegister(), ldrDestReg),
                B(printFun, link = true)
            )
        ).apply {
            if (ctx.newlineAfter) {
                return this.plus(B(P_PRINT_LN, link = true))
            }
        }
    }

    private fun visitDeclarationStat(ctx: Declaration): List<WInstruction> {
        // Visit RHS. Result should be in resultStored register.
        return RHSVisitor(data, registerProvider, funcPool).visit(ctx.rhs).plus(
            ctx.st.asmAssign(ctx.identifier, Register.resultRegister(), data, ctx.decType)
        )
    }

    private fun visitAssignStat(ctx: Assignment): List<WInstruction> {
        // Visit RHS. Result should be in resultStored register.
        return RHSVisitor(data, registerProvider, funcPool).visit(ctx.rhs).plus(
            when (ctx.lhs) {
                is IdentifierSet -> ctx.st.asmAssign(
                    ctx.lhs.identifier,
                    Register.resultRegister(),
                    data,
                    null
                )
                is ArrayElement -> {
                    // when assigning an array element it is important to remain inside the bounds
                    pCheckArrayBounds(data, funcPool)

                    ctx.st.asmAssign(
                            ctx.lhs.identifier,
                            ctx.lhs.indices,
                            Register.resultRegister(),
                            data,
                            registerProvider,
                            funcPool
                        )
                }
                is PairElement -> TODO("Pair elements assignments not yet implemented")
                else -> throw Exception("An LHS is not one of the three possible ones...what?")
            }
        )
    }


}
