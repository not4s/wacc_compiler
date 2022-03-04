package codegen

import ast.ArrayElement
import ast.IdentifierSet
import ast.LHS
import ast.PairElement
import instructions.WInstruction
import instructions.misc.DataDeclaration
import instructions.misc.Operand2
import instructions.misc.Register
import instructions.operations.MOV
import instructions.operations.POP
import instructions.operations.PUSH
import instructions.operations.STR

class LHSVisitor(
    private val data: DataDeclaration,
    private val registerProvider: RegisterProvider,
    private val funcPool: FunctionPool,
) : ASTVisitor<LHS> {

    var resultStored: Operand2? = null

    override fun visit(ctx: LHS): List<WInstruction> {
        return when (ctx) {
            is IdentifierSet -> {
                ctx.st.asmAssign(ctx.identifier, Register.resultRegister(), data, null)
            }
            is ArrayElement -> TODO()

            is PairElement -> {
                return listOf<WInstruction>(
                    PUSH(Register.resultRegister(), data),
                ).plus(ExprVisitor(data, registerProvider, funcPool).visit(ctx.expr))
                    .plus(POP(Register("r1"), data)).plus(
                        STR(Register.resultRegister(), Register.resultRegister(), offset = if (ctx.first) 0 else 4)

                    ).plus(
                        MOV(Register.resultRegister(), Register("r1"))
                    )
            }
            else -> throw Exception("Unknown LHS $ctx")
        }
    }
}
