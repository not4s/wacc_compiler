package codegen

import ast.Literal
import ast.RHS
import instructions.WInstruction
import instructions.misc.Immediate
import instructions.misc.Register
import instructions.operations.LDR
import waccType.WBool
import waccType.WInt

class RHSVisitor : ASTVisitor<RHS> {
    // Stores result of visiting in R4.
    override fun visit(ctx: RHS): List<WInstruction> {
        return when (ctx) {
            is Literal -> visitLiteral(ctx)
            else -> TODO("Not yet implemented")
        }
    }

    private fun visitLiteral(ctx: Literal): List<WInstruction> {
        return when (ctx.type) {
            is WInt -> listOf(LDR(Register("r4"), Immediate(ctx.type.value!!)))
            is WBool -> listOf(LDR(Register("r4"), Immediate(
                if (ctx.type.value!!) {
                    1
                } else 0
            )))
            else -> TODO("Not yet implemented")
        }
    }


}
