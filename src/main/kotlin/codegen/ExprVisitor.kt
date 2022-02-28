package codegen

import ast.Expr
import ast.IdentifierGet
import ast.Literal
import instructions.WInstruction
import instructions.misc.Immediate
import instructions.misc.Operand2
import instructions.misc.Register
import waccType.WInt

class ExprVisitor(val registerProvider: RegisterProvider) : ASTVisitor<Expr> {

    var resultStored: Operand2? = null

    override fun visit(ctx: Expr): List<WInstruction> {
        return when (ctx) {
            is Literal -> when (ctx.type) {
                is WInt -> {
                    val exitCode = ctx.type.value ?: throw Exception("Exit code not found")
                    resultStored = Immediate(exitCode)
                    listOf()
                }
                else -> throw Exception("Somehow not an int exit")
            }
            is IdentifierGet -> {
                val reg = registerProvider.get()
                resultStored = reg
                ctx.st.asmGet(ctx.identifier, reg)
            }
            else -> TODO("Not implemented yet")
        }
    }
}
