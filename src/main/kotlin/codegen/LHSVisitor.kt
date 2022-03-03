package codegen

import ast.ArrayElement
import ast.IdentifierSet
import ast.LHS
import ast.PairElement
import instructions.operations.*
import instructions.WInstruction
import instructions.misc.DataDeclaration
import instructions.misc.Operand2

class LHSVisitor(
    private val data: DataDeclaration,
    private val registerProvider: RegisterProvider,
    private val funcPool: FunctionPool,
    ) : ASTVisitor<LHS> {

    var resultStored: Operand2? = null

    override fun visit(ctx: LHS): List<WInstruction> {
        return when (ctx) {
            is IdentifierSet -> {
                val freeRegister = registerProvider.get()
                resultStored = freeRegister
                registerProvider.ret()
                ctx.st.asmGet(ctx.identifier, freeRegister, data)
            }
            is ArrayElement -> TODO()

            is PairElement -> {
                val exprReg = registerProvider.get()
                val pairElemReg = registerProvider.get()
                return ExprVisitor(data, registerProvider, funcPool).visit(ctx.expr).
                        plus(STR(exprReg, pairElemReg, 0))
            }
            else -> throw Exception("Unknown LHS $ctx")
        }
    }
}
