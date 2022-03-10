package codegen

import ast.ArrayElement
import ast.IdentifierSet
import ast.LHS
import ast.PairElement
import instructions.WInstruction
import instructions.misc.DataDeclaration
import instructions.misc.ImmediateOffset
import instructions.misc.Register
import instructions.operations.*

class LHSVisitor(
    private val data: DataDeclaration,
    private val registerProvider: RegisterProvider,
    private val funcPool: FunctionPool,
) : ASTVisitor<LHS> {

    override fun visit(ctx: LHS): List<WInstruction> {
        return when (ctx) {
            is IdentifierSet -> {
                ctx.st.asmAssign(ctx.identifier, Register.resultRegister(), data, null)
            }
            is ArrayElement -> {
                // when assigning an array element it is important to remain inside the bounds
                pCheckArrayBounds(data, funcPool)

                ctx.st.asmAssign(
                    ctx.identifier,
                    ctx.indices,
                    Register.resultRegister(),
                    data,
                    registerProvider,
                    funcPool
                )
            }

            is PairElement -> {
                pCheckNullPointer(data, funcPool)
                return listOf<WInstruction>(
                    PUSH(Register.resultRegister(), data),
                ).plus(ExprVisitor(data, registerProvider, funcPool).visit(ctx.expr))
                    .plus(B(CHECK_NULL_POINTER, link = true)).plus(POP(Register("r1"), data)).plus(
                        LDR(
                            Register.resultRegister(),
                            ImmediateOffset(
                                Register.resultRegister(),
                                offset = if (ctx.first) 0 else 4
                            )
                        )

                    ).plus(
                        STR(Register("r1"), Register.resultRegister())
                    )
            }
        }
    }
}
