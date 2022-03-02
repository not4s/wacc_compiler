package codegen

import ast.*
import ast.statement.*
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.B
import instructions.operations.LDR
import instructions.operations.MOV
import utils.btoi
import waccType.WBool
import waccType.WChar
import waccType.WInt
import waccType.WStr

class StatVisitor(
    val data: DataDeclaration,
    private val funcPool: FunctionPool,
) : ASTVisitor<Stat> {

    val registerProvider = RegisterProvider()

    override fun visit(ctx: Stat): List<WInstruction> {
        return when (ctx) {
            is SkipStat -> listOf()
            is ExitStat -> visitExitStat(ctx)
            is Declaration -> visitDeclarationStat(ctx)
            is Assignment -> visitAssignStat(ctx)
            is JoinStat -> visit(ctx.first).plus(visit(ctx.second))
            is PrintStat -> visitPrintStat(ctx)
            else -> TODO("Not yet implemented")
        }
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
        when (ctx.expr.type) {
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
        return evalExprInstructions.plus(listOf(
            firstArgInitInstruction,
            MOV(Register.resultRegister(), ldrDestReg),
            B(printFun, link = true)
        )).apply {
            if (ctx.newlineAfter) {
                return this.plus(B(P_PRINT_LN, link = true))
            }
        }
    }

    private fun visitDeclarationStat(ctx: Declaration): List<WInstruction> {
        // Visit RHS. Result should be in resultStored register.
        return RHSVisitor(data, registerProvider, funcPool).visit(ctx.rhs).plus(
            ctx.st.asmAssign(ctx.identifier, Register.resultRegister(), data)
        )
    }

    private fun visitAssignStat(ctx: Assignment): List<WInstruction> {
        // Visit RHS. Result should be in resultStored register.
        return RHSVisitor(data, registerProvider, funcPool).visit(ctx.rhs).plus(
            when (ctx.lhs) {
                is IdentifierSet -> ctx.st.asmAssign(ctx.lhs.identifier, Register.resultRegister(), data)
                is ArrayElement -> TODO("Array elements assignments not yet implemented")
                is PairElement -> TODO("Pair elements assignments not yet implemented")
                else -> throw Exception("An LHS is not one of the three possible ones...what?")
            }
        )
    }


}


