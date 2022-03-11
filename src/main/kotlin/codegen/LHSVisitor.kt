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
import codegen.CHECK_NULL_POINTER

class LHSVisitor(
    private val data: DataDeclaration,
    private val registerProvider: RegisterProvider,
    private val funcPool: FunctionPool,
) : ASTVisitor<LHS> {

    override fun visit(ctx: LHS): List<WInstruction> {
        return when (ctx) {
            is IdentifierSet -> {
                ctx.st.asmAssign(ctx.identifier, Register.R0, data, null)
            }
            is ArrayElement -> {
                // when assigning an array element it is important to remain inside the bounds
                pCheckArrayBounds(data, funcPool)

                ctx.st.asmAssign(
                    ctx.identifier,
                    ctx.indices,
                    Register.R0,
                    data,
                    registerProvider,
                    funcPool
                )
            }

            is PairElement -> {
                pCheckNullPointer(data, funcPool)
                return listOf<WInstruction>(
                    PUSH(Register.R0, data),
                ).plus(ExprVisitor(data, registerProvider, funcPool).visit(ctx.expr))
                    .plus(B(CHECK_NULL_POINTER)).plus(POP(Register.R1, data)).plus(
                        LDR(
                            Register.R0,
                            ImmediateOffset(
                                Register.R0,
                                offset = if (ctx.first) 0 else 4
                            )
                        )

                    ).plus(
                        STR(Register.R1, Register.R0)
                    )
            }
        }
    }
}
