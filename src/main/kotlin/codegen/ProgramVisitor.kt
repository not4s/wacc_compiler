package codegen

import ast.ProgramAST
import instructions.operations.LDR
import instructions.operations.POP
import instructions.operations.PUSH
import instructions.WInstruction
import instructions.aux.*

class ProgramVisitor : ASTVisitor<ProgramAST> {

    override fun visit(ctx: ProgramAST): List<WInstruction> {
        return listOf(
            Section(".text"),
            BlankLine(),
            Section(".global", "main"),
            Label("main")
        ).plus(
            PUSH(Register.linkRegister())
        ).plus(
            StatVisitor().visit(ctx.body)
        ).plus(listOf(
            LDR(Register("r0"), Immediate(0)),
            POP(Register.programCounter()),
            LTORG()
        ))
    }
}