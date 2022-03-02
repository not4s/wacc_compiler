package codegen

import ast.ArrayElement
import ast.IdentifierSet
import ast.LHS
import ast.PairElement
import instructions.WInstruction
import instructions.misc.DataDeclaration
import instructions.misc.Operand2

class LHSVisitor(
    private val registerProvider: RegisterProvider,
    private val data: DataDeclaration
) : ASTVisitor<LHS> {

    var resultStored: Operand2? = null

    override fun visit(ctx: LHS): List<WInstruction> {
        return when (ctx) {
            is IdentifierSet -> {
                val freeRegister = registerProvider.get()
                resultStored = freeRegister
                ctx.st.asmGet(ctx.identifier, freeRegister, data)
            }
            is ArrayElement -> TODO()
            is PairElement -> TODO()
            else -> throw Exception("Unknown LHS $ctx")
        }
    }
}
