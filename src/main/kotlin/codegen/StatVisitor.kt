package codegen

import ast.Literal
import ast.Stat
import ast.statement.*
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*
import utils.btoi
import waccType.WBool
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
        val firstArgInitInstruction: WInstruction = if (printFun == P_PRINT_BOOL) {
            MOV(ldrDestReg, Immediate(btoi(literal == LITERAL_TRUE)))
        } else {
            LDR(ldrDestReg, LabelReference(literal, data))
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
        // Visit RHS, result will be stored in r4.
        return RHSVisitor().visit(ctx.rhs).plus(
            ctx.st.asmAssign(ctx.identifier, Register("r4"))
        )
    }

}


