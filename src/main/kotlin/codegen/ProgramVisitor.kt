package codegen


import ast.ProgramAST
import ast.Stat
import ast.WACCFunction
import ast.statement.ReturnStat
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.PUSH


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

        val program = visitBody(ctx.body, "main", listOf())

        val functionDeclarations: List<WInstruction> = ctx.functions.map { visitFunc(it) }.flatten()

        return (data.takeUnless { it.isEmpty() }?.getInstructions() ?: listOf())
            .plus(programInitialisation)
            .plus(functionDeclarations)
            .plus(program)
            .plus(funcPool.flatten())
    }

    private fun visitFunc(ctx: WACCFunction): List<WInstruction> {
        return visitBody(ctx.body, funcLabel(ctx.identifier), listOf())
    }

    /**
     * If visitBody() function gets main, then it treats it as having return 0 at the end
     */
    private fun visitBody(
        body: Stat,
        funcName: String,
        paramsAssign: List<WInstruction>
    ): List<WInstruction> {
        return listOf(Label(funcName))
            .asSequence()
            .plus(paramsAssign)
            .plus(PUSH(Register.LR))
            .plus(offsetStackBy(body.st.totalByteSize)) // Offset initial SP
            .plus(StatVisitor(data, funcPool, body.st.totalByteSize).visit(body)).plus(
                if (funcName == "main")
                    StatVisitor(data, funcPool, body.st.totalByteSize).visit(ReturnStat.zero())
                else
                    listOf()
            )
            .toList()
    }
}