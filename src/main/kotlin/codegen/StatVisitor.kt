package codegen

import ast.Stat
import ast.statement.ExitStat
import ast.statement.SkipStat
import instructions.WInstruction
import instructions.aux.DataDeclaration
import instructions.aux.Operand2
import instructions.aux.Register
import instructions.operations.B
import instructions.operations.LDR
import instructions.operations.MOV

class StatVisitor : ASTVisitor<Stat> {

    val registerProvider = RegisterProvider()

    private fun visitExitStat(ctx: ExitStat, data: DataDeclaration): List<WInstruction> {
        val exprVisitor: ExprVisitor = ExprVisitor(registerProvider)
        val evaluationCode = exprVisitor.visit(ctx.expr, data)
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

    override fun visit(ctx: Stat, data: DataDeclaration): List<WInstruction> {
        return when (ctx) {
            is SkipStat -> listOf()
            is ExitStat -> visitExitStat(ctx, data)
            else -> TODO("Not yet implemented")
        }
    }
}