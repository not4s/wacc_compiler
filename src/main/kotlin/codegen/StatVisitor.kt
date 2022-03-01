package codegen

import ast.*
import ast.statement.*
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*
import utils.btoi
import waccType.WBool
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
        val exprVisitor = ExprVisitor(registerProvider)
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
        if (ctx.expr is Literal) {
            when (ctx.expr.type) {
                is WStr -> {
                    literal = ctx.expr.type.value ?: throw Exception("Unspecified Literal string")
                    printFun = P_PRINT_STRING
                    data.addDeclaration(literal)
                    data.addDeclaration(NULL_TERMINAL_STRING)
                    funcPool.add(pPrintString(data))
                }
                is WBool -> {
                    literal = ctx.expr.type.value.toString() + NULL_CHAR
                    printFun = P_PRINT_BOOL
                    data.addDeclaration(LITERAL_TRUE)
                    data.addDeclaration(LITERAL_FALSE)
                    funcPool.add(pPrintBool(data))
                }
                is WInt -> {
                    literal = ctx.expr.type.value.toString()
                    printFun = P_PRINT_INT
                    data.addDeclaration(NULL_TERMINAL_INT)
                    funcPool.add(pPrintInt(data))
                }
                else -> TODO("Non-String literals are not supported. They will require things like %s %d etc")
            }
        } else {
            TODO("expression evaluation and argument passing is not complete")
            // val exprVisitor = ExprVisitor(registerProvider)
            // val evaluatedExpr = exprVisitor.visit(ctx.expr, data)
            // val exitCode: Operand2 = exprVisitor.resultStored ?: throw Exception("Unhandled result")
        }
        if (ctx.newlineAfter) {
            data.addDeclaration(NULL_CHAR)
            funcPool.add(pPrintLn(data))
        }
        val ldrDestReg = registerProvider.get()
        val firstArgInitInstruction: WInstruction =
            when (printFun) {
                P_PRINT_BOOL -> MOV(ldrDestReg, Immediate(btoi(literal == LITERAL_TRUE)))
                P_PRINT_INT -> MOV(ldrDestReg, Immediate(literal.toInt()))
                P_PRINT_STRING -> LDR(ldrDestReg, LabelReference(literal, data))
                else -> TODO("Not yet implemented")
            }

        return listOf(
            firstArgInitInstruction,
            MOV(Register.resultRegister(), ldrDestReg),
            B(printFun, link = true)
        ).apply {
            if (ctx.newlineAfter) {
                return this.plus(B(P_PRINT_LN, link = true))
            }
        }
    }

    private fun visitDeclarationStat(ctx: Declaration): List<WInstruction> {
        // Visit RHS. Result should be in resultStored register.
        return RHSVisitor().visit(ctx.rhs).plus(
            ctx.st.asmAssign(ctx.identifier, Register.resultRegister())
        )
    }
    private fun visitAssignStat(ctx: Assignment): List<WInstruction> {
        // Visit RHS. Result should be in resultStored register.
        return RHSVisitor().visit(ctx.rhs).plus(
            when (ctx.lhs) {
                is IdentifierSet -> ctx.st.asmAssign(ctx.lhs.identifier, Register.resultRegister())
                is ArrayElement -> TODO("Array elements assignments not yet implemented")
                is PairElement -> TODO("Pair elements assignments not yet implemented")
                else -> throw Exception("An LHS is not one of the three possible ones...what?")
            }
        )
    }


}


