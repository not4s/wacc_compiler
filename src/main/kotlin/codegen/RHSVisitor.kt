package codegen

import ast.Literal
import ast.RHS
import instructions.WInstruction
import instructions.misc.ImmediateChar
import instructions.misc.LoadImmediate
import instructions.misc.Register
import instructions.operations.LDR
import instructions.operations.MOV
import waccType.WBool
import waccType.WChar
import waccType.WInt

// Stores visiting result in Register.resultRegister.
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
            is WInt -> listOf(LDR(Register.resultRegister(), LoadImmediate(ctx.type.value!!)))
            is WBool -> listOf(LDR(Register.resultRegister(), LoadImmediate(
                if (ctx.type.value!!) {
                    1
                } else 0)))
            is WChar -> listOf(MOV(Register.resultRegister(), ImmediateChar(ctx.type.value!!)))
            else -> TODO("Not yet implemented")
        }
    }


}
