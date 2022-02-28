package codegen

import ast.Stat
import ast.statement.SkipStat
import instructions.WInstruction

class StatVisitor : ASTVisitor<Stat> {
    override fun visit(ctx: Stat): List<WInstruction> {
        if (ctx is SkipStat) {
            return listOf()
        }
        TODO("Not yet implemented")
    }
}