package codegen

import ast.ProgramAST
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*

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
            // Offset initial SP
            offsetStackBy(ctx.body.st.totalByteSize)
        ).plus(
            StatVisitor().visit(ctx.body)
        ).plus(
            // Un-offset initial SP
            unOffsetStackBy(ctx.body.st.totalByteSize)
        ).plus(listOf(
            LDR(Register("r0"), Immediate(0)),
            POP(Register.programCounter()),
            LTORG()
        ))
    }
}