package codegen

import ast.Literal
import ast.RHS
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.LDR
import instructions.operations.MOV
import waccType.WBool
import waccType.WChar
import waccType.WInt
import waccType.WStr

// Stores visiting result in Register.resultRegister.
class RHSVisitor(val data: DataDeclaration) : ASTVisitor<RHS> {
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
            is WStr -> {
                data.addDeclaration(ctx.type.value!!)
                listOf(
                    LDR(Register.resultRegister(), LabelReference(ctx.type.value, data)),
                )
            }
            else -> TODO("Not yet implemented")
        }
    }


}
