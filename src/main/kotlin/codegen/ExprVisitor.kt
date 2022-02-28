package codegen

import ast.Expr
import ast.Literal
import instructions.WInstruction
import instructions.misc.Immediate
import instructions.misc.Operand2
import waccType.WInt

class ExprVisitor(registerProvider: RegisterProvider) : ASTVisitor<Expr> {

    var resultStored: Operand2? = null

    override fun visit(ctx: Expr): List<WInstruction> {
        if (ctx is Literal) {
            when (ctx.type) {
                is WInt -> {
                    val exitCode = ctx.type.value ?: throw Exception("Exit code not found")
                    resultStored = Immediate(exitCode)
                    return listOf()
                }
                else -> throw Exception("Somehow not an int exit")
            }
        }
        TODO("Not implemented yet")
    }
}
