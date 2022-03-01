package codegen

import ast.Literal
import ast.Stat
import ast.statement.*
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.B
import instructions.operations.LDR
import instructions.operations.MOV
import waccType.WStr

class StatVisitor(
    val data: DataDeclaration,
    private val funcPool: MutableList<List<WInstruction>>,
) : ASTVisitor<Stat> {

    val registerProvider = RegisterProvider()

    override fun visit(ctx: Stat): List<WInstruction> {
        return when (ctx) {
            is SkipStat -> listOf()
            is ExitStat -> visitExitStat(ctx)
            is Declaration -> visitDeclarationStat(ctx)
            is JoinStat -> visit(ctx.first).plus(visit(ctx.second))
            else -> TODO("Not yet implemented")
        }
    }

    private fun visitExitStat(ctx: ExitStat): List<WInstruction> {
        val exprVisitor = ExprVisitor(registerProvider)
        val evaluationCode = exprVisitor.visit(ctx.expr)
        val exitCodeLoadable: Loadable = when (val operand2 = exprVisitor.resultStored) {
            is Loadable -> operand2
            is Immediate -> operand2.asLoadable()
            else -> throw Exception("Unknown Operand2 when visiting Exit Statement")
        }
        val ldrDestReg = registerProvider.get()
        return evaluationCode.plus(
            listOf(
                LDR(ldrDestReg, exitCodeLoadable),
                MOV(Register.resultRegister(), ldrDestReg),
                B("exit", B.Condition.L)
            )
        )
    }

    private fun visitPrintStat(ctx: PrintStat): List<WInstruction> {
        val literal: String
        if (ctx.expr is Literal) {
            literal = when (ctx.expr.type) {
                is WStr -> ctx.expr.type.value ?: throw Exception("Unspecified Literal string")
                else -> TODO("Non-String literals are not supported. They will require things like %s %d etc")
            }
        } else {
            TODO("expression evaluation and argument passing is not complete")
            // val exprVisitor = ExprVisitor(registerProvider)
            // val evaluatedExpr = exprVisitor.visit(ctx.expr, data)
            // val exitCode: Operand2 = exprVisitor.resultStored ?: throw Exception("Unhandled result")
        }
        data.addDeclaration(literal)
        data.addDeclaration(NULL_TERMINAL_STRING)
        funcPool.add(pPrintString(data))
        if (ctx.newlineAfter) {
            data.addDeclaration(NULL_CHAR)
            funcPool.add(pPrintLn(data))
        }
        val ldrDestReg = registerProvider.get()
        return listOf(
            LDR(ldrDestReg, LabelReference(literal, data)),
            MOV(Register.resultRegister(), ldrDestReg),
            B(P_PRINT_STRING, B.Condition.L)
        ).apply {
            if (ctx.newlineAfter) {
                return this.plus(B(P_PRINT_LN, B.Condition.L))
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


