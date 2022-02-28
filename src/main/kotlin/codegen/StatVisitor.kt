package codegen

import ast.Stat
import ast.statement.Declaration
import ast.statement.ExitStat
import ast.statement.JoinStat
import ast.statement.SkipStat
import instructions.WInstruction
import instructions.misc.Immediate
import instructions.misc.Operand2
import instructions.misc.Register
import instructions.operations.B
import instructions.operations.LDR
import instructions.operations.MOV

class StatVisitor : ASTVisitor<Stat> {

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
        val exprVisitor: ExprVisitor = ExprVisitor(registerProvider)
        val evaluationCode = exprVisitor.visit(ctx.expr)
        val exitCode: Operand2 = exprVisitor.resultStored ?: throw Exception("Unhandled res")
        val ldrDestReg = registerProvider.get()
        return evaluationCode.plus(
            listOf(
                when (exitCode) {
                    is Immediate -> LDR(ldrDestReg, exitCode)
                    is Register -> MOV(ldrDestReg, exitCode)
                    else -> TODO("Not yet implemented")
                },
                MOV(Register.resultRegister(), ldrDestReg),
                B("exit", B.Condition.L)
            )
        )
    }

}

private fun visitDeclarationStat(ctx: Declaration): List<WInstruction> {
    // Visit RHS, result will be stored in r4.
    return RHSVisitor().visit(ctx.rhs).plus(
        ctx.st.asmAssign(ctx.identifier, Register("r4"))
    )
}
