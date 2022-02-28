package codegen

import ast.ProgramAST
import instructions.WInstruction
import instructions.aux.*
import instructions.operations.LDR
import instructions.operations.POP
import instructions.operations.PUSH

class ProgramVisitor : ASTVisitor<ProgramAST> {

    override fun visit(ctx: ProgramAST, data: DataDeclaration): List<WInstruction> {
        val programInitialisation = listOf(
            Section(".text"),
            BlankLine(),
            Section(".global", "main"),
            Label("main")
        )

        val program = listOf(PUSH(Register.linkRegister()))
            .plus(
                StatVisitor().visit(ctx.body, data)
            ).plus(
                listOf(
                    LDR(Register("r0"), Immediate(0)),
                    POP(Register.programCounter()),
                    LTORG()
                )
            )

        return data.getInstructions().plus(programInitialisation).plus(program)
    }
}