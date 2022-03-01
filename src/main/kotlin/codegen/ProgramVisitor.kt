package codegen


import ast.ProgramAST
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*


class ProgramVisitor(
    val data: DataDeclaration,
    private val funcPool: MutableList<List<WInstruction>> = mutableListOf(),
) : ASTVisitor<ProgramAST> {

    override fun visit(ctx: ProgramAST): List<WInstruction> {
        val programInitialisation = listOf(
            Section(".text"),
            BlankLine(),
            Section(".global", "main"),
            Label("main")
        )

        val program = listOf(PUSH(Register.linkRegister()))
            .plus(
                // Offset initial SP
                offsetStackBy(ctx.body.st.totalByteSize))
            .plus(
                StatVisitor(data, funcPool).visit(ctx.body)
            ).plus(
                // Un-offset initial SP
                unOffsetStackBy(ctx.body.st.totalByteSize)
            ).plus(
                listOf(
                    LDR(Register("r0"), LoadImmediate(0)),
                    POP(Register.programCounter()),
                    LTORG()
                )
            )

        return (data.takeUnless { it.isEmpty() }?.getInstructions() ?: listOf())
            .plus(programInitialisation)
            .plus(program)
            .plus(funcPool.flatten())
    }
}