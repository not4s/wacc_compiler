package codegen


import ast.ProgramAST
import ast.Stat
import ast.statement.ReturnStat
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*


class ProgramVisitor(
    val data: DataDeclaration,
    private val funcPool: FunctionPool = FunctionPool(),
) : ASTVisitor<ProgramAST> {

    override fun visit(ctx: ProgramAST): List<WInstruction> {
        val programInitialisation = listOf(
            Section(".text"),
            BlankLine(),
            Section(".global", "main"),
        )

        val functionDeclarations: List<WInstruction> = ctx.functions.map {
            visitBody(it.body, "f_" + it.identifier)
        }.flatten()

//        val program = listOf(Label("main"), PUSH(Register.linkRegister()))
//            .plus(
//                // Offset initial SP
//                offsetStackBy(ctx.body.st.totalByteSize))
//            .plus(
//                StatVisitor(data, funcPool).visit(ctx.body)
//            ).plus(
//                // Un-offset initial SP
//                unOffsetStackBy(ctx.body.st.totalByteSize)
//            ).plus(
//                listOf(
//                    LDR(Register("r0"), LoadImmediate(0)),
//                    POP(Register.programCounter()),
//                    LTORG()
//                )
//            )
        val program = visitBody(ctx.body, "main")

        return (data.takeUnless { it.isEmpty() }?.getInstructions() ?: listOf())
            .plus(programInitialisation)
            .plus(functionDeclarations)
            .plus(program)
            .plus(funcPool.flatten())
    }

    /**
     * If visitBody() function gets main, then it treats it as having return 0 at the end
     */
    private fun visitBody(body: Stat, funcName: String): List<WInstruction> {
        return listOf(Label(funcName), PUSH(Register.linkRegister()))
            .plus(
                // Offset initial SP
                offsetStackBy(body.st.totalByteSize))
            .plus(
                StatVisitor(data, funcPool, body.st.totalByteSize).visit(body)
            ).plus(
                if (funcName == "main")
                    StatVisitor(data, funcPool, body.st.totalByteSize).visit(ReturnStat.zero())
                else
                    listOf()
            )
//            .plus(
//                // Un-offset initial SP
//                unOffsetStackBy(body.st.totalByteSize)
//            ).plus(
//                // TODO("Move this into visit ReturnStat")
//                listOf(
//                    LDR(Register("r0"), LoadImmediate(0)),
//                    POP(Register.programCounter()),
//                    LTORG()
//                )
//            )
    }
}