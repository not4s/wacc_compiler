package codegen

import ast.ProgramAST
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.LDR
import instructions.operations.POP
import instructions.operations.PUSH

class ProgramVisitor(
    val data: DataDeclaration,
    private val funcPool: MutableList<List<WInstruction>> = mutableListOf()
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
                StatVisitor(data, funcPool).visit(ctx.body)
            ).plus(
                listOf(
                    LDR(Register("r0"), LoadImmediate(0)),
                    POP(Register.programCounter()),
                    LTORG()
                )
            )

        return data.getInstructions()
            .plus(programInitialisation)
            .plus(program)
            .plus(funcPool.flatten())
    }
}