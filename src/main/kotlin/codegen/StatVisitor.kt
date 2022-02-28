package codegen

import ast.Stat
import ast.statement.ExitStat
import ast.statement.SkipStat
import instructions.WInstruction
import instructions.misc.Operand2
import instructions.misc.Register
import instructions.operations.B
import instructions.operations.LDR
import instructions.operations.MOV

class StatVisitor : ASTVisitor<Stat> {

    private val registerProvider = RegisterProvider()

    override fun visit(ctx: Stat): List<WInstruction> {
        return when (ctx) {
            is SkipStat -> listOf()
            is ExitStat -> visitExitStat(ctx)
            else -> TODO("Not yet implemented")
        }
    }

    private fun visitExitStat(ctx: ExitStat): List<WInstruction> {
        val exprVisitor = ExprVisitor(registerProvider)
        val evaluationCode = exprVisitor.visit(ctx.expr)
        val exitCode: Operand2 = exprVisitor.resultStored ?: throw Exception("Unhandled res")
        val ldrDestReg = registerProvider.get()
        return evaluationCode.plus(
            listOf(
                LDR(ldrDestReg, exitCode),
                MOV(Register.resultRegister(), ldrDestReg),
                B("exit", B.Condition.L)
            )
        )
    }
}