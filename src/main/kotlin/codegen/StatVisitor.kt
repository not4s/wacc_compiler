package codegen

import ast.IdentifierSet
import ast.Literal
import ast.Stat
import ast.WACCStruct
import ast.statement.*
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*
import symbolTable.typeToByteSize
import utils.btoi
import waccType.*

class StatVisitor(
    val data: DataDeclaration,
    private val funcPool: FunctionPool,
    private val returnUnOffsetByteSize: Int? = null
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
                    withScope(ctx.first.st.totalByteSize, visit(ctx.first))
                } else {
                    visit(ctx.first)
                }.plus(
                    if (ctx.second.st !== ctx.st) {
                        withScope(ctx.second.st.totalByteSize, visit(ctx.second))
                    } else {
                        visit(ctx.second)
                    }
                )
            }
            is PrintStat -> visitPrintStat(ctx)
            is ReadStat -> visitReadStat(ctx)
            is IfThenStat -> visitIfThenStat(ctx)
            is WhileStat -> visitWhileStat(ctx)
            is ReturnStat -> visitReturnStat(ctx)
            is FreeStat -> visitFreeStat(ctx)
            is StructDeclarationStat -> visitDeclarationStat(ctx)
            else -> TODO("Unrecognized Statement ${ctx::class}")
        }
    }

    private fun visitDeclarationStat(ctx: StructDeclarationStat): List<WInstruction> {
        // calculate size of struct
        val sizeOfStruct: Int = (ctx.type as WACCStruct).params.entries.sumOf { (_, type) -> typeToByteSize(type) }
        return listOf(LDR(Register.R0, LoadImmediate(sizeOfStruct)), B(MALLOC))
    }

    private fun visitFreeStat(ctx: FreeStat): List<WInstruction> {
        pCheckNullPointer(data, funcPool)
        val exprVisitor = ExprVisitor(data, registerProvider, funcPool)
        val evaluationCode = exprVisitor.visit(ctx.expression)
        return evaluationCode.plus(B(CHECK_NULL_POINTER)).plus(
            B("free")
        )
    }

    private fun visitReturnStat(ctx: ReturnStat): List<WInstruction> {
        val exprVisitor = ExprVisitor(data, registerProvider, funcPool)
        val evaluationCode = exprVisitor.visit(ctx.expression)
        return evaluationCode
            .plus(
                unOffsetStackBy(
                    returnUnOffsetByteSize ?: throw Exception("Cannot restore stack offset")
                )
            )
            .plus(
                listOf(
                    MOV(Register.R0, Register.R0),
                    POP(Register.PC),
                    LTORG()
                )
            )
    }

    private fun visitWhileStat(ctx: WhileStat): List<WInstruction> {
        val whileStartLabel: String = funcPool.getAbstractLabel()
        val whileBodyLabel: String = funcPool.getAbstractLabel()

        val whileBody: List<WInstruction> = withScope(
            ctx.doBlock.st.totalByteSize,
            StatVisitor(data, funcPool, returnUnOffsetByteSize).visit(ctx.doBlock)
        )

        val jump = B(whileBodyLabel, cond = B.Condition.NE)

        val exprVisitor = ExprVisitor(data, registerProvider, funcPool)
        val condition = exprVisitor.visit(ctx.condition)

        return listOf(B(whileStartLabel))
            .asSequence()
            .plus(Label(whileBodyLabel))
            .plus(whileBody)
            .plus(Label(whileStartLabel))
            .plus(condition)
            .plus(CMP(Register.R0, Immediate(0)))
            .plus(jump)
            .toList()
    }

    private fun visitReadStat(ctx: ReadStat): List<WInstruction> {
        val output = mutableListOf<WInstruction>()
        val readFun: String
        when (ctx.lhs.type) {
            is WInt -> {
                readFun = P_READ_INT
                data.addDeclaration(NULL_TERMINAL_INT)
                funcPool.add(pReadInt(data))
            }
            is WChar -> {
                readFun = P_READ_CHAR
                data.addDeclaration(NULL_TERMINAL_CHAR)
                funcPool.add(pReadChar(data))
            }
            else -> throw Exception("Can only read chars or ints")
        }

        return output.asSequence().plus(PUSH(Register.R0))
            .plus(MOV(Register.R0, Register.SP))
            .plus(
                B(readFun)
            ).plus(POP(Register.R0))
            .plus(
                when (ctx.lhs) {
                    is IdentifierSet -> ctx.st.asmAssign(
                        ctx.lhs.identifier,
                        Register.R0,
                        data,
                        null
                    )
                    else -> TODO()
                }
            ).toList()

    }

    private fun visitIfThenStat(ctx: IfThenStat): List<WInstruction> {
        // evaluate conditional
        val exprVisitor = ExprVisitor(data, registerProvider, funcPool)
        val condition = exprVisitor.visit(ctx.condition)

        val elseBranchLabel: String = funcPool.getAbstractLabel()
        val afterIfLabel: String = funcPool.getAbstractLabel()

        val thenCode: List<WInstruction> = withScope(
            ctx.thenStat.st.totalByteSize,
            StatVisitor(data, funcPool, returnUnOffsetByteSize).visit(ctx.thenStat)
        )
        val elseCode: List<WInstruction> = withScope(
            ctx.elseStat.st.totalByteSize,
            StatVisitor(data, funcPool, returnUnOffsetByteSize).visit(ctx.elseStat)
        )

        // compare with false, branch if equal (else branch)
        val jump = listOf(
            CMP(Register.R0, Immediate(0)),
            B(elseBranchLabel, cond = B.Condition.EQ)
        )

        return condition
            .asSequence()
            .plus(jump)
            .plus(thenCode)
            .plus(B(afterIfLabel))
            .plus(Label(elseBranchLabel))
            .plus(elseCode)
            .plus(Label(afterIfLabel))
            .toList()
    }

    private fun visitExitStat(ctx: ExitStat): List<WInstruction> {
        val exprVisitor = ExprVisitor(data, registerProvider, funcPool)
        val evaluationCode = exprVisitor.visit(ctx.expr)
        return evaluationCode.plus(
            listOf(
                MOV(Register.R0, Register.R0),
                B("exit")
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
                    funcPool.add(pPrintString(data))
                } else {
                    printFun = P_PRINT_REFERENCE
                    pPrintReference(data, funcPool)
                }
            }
            is WPair -> {
                printFun = P_PRINT_REFERENCE
                funcPool.add(pPrintReference(data))
            }
            is WPairNull -> {
                printFun = P_PRINT_REFERENCE
                data.addDeclaration(NULL_TERMINAL_POINTER)
                funcPool.add(pPrintReference(data))
            }
            else -> TODO("Print stat visitor not impl. Context is $ctx and type is $type")
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
            firstArgInitInstruction = MOV(ldrDestReg, Register.R0)
        }

        if (ctx.newlineAfter) {
            data.addDeclaration(NULL_CHAR)
            funcPool.add(pPrintLn(data))
        }

        registerProvider.ret()
        return evalExprInstructions.plus(
            listOf(
                firstArgInitInstruction,
                MOV(Register.R0, ldrDestReg),
                B(printFun)
            )
        ).apply {
            if (ctx.newlineAfter) {
                return this.plus(B(P_PRINT_LN))
            }
        }
    }

    private fun visitDeclarationStat(ctx: Declaration): List<WInstruction> {
        // Visit RHS. Result should be in resultStored register.
        return RHSVisitor(data, registerProvider, funcPool).visit(ctx.rhs)
            .plus(
                ctx.st.asmAssign(ctx.identifier, Register.R0, data, ctx.decType)
            )
    }

    private fun visitAssignStat(ctx: Assignment): List<WInstruction> {
        return RHSVisitor(data, registerProvider, funcPool, ctx.lhs).visit(ctx.rhs)
            .plus(LHSVisitor(data, registerProvider, funcPool).visit(ctx.lhs))
    }
}
