package codegen

import ast.Stat
import instructions.WInstruction

class StatVisitor : ASTVisitor<Stat> {
    override fun visit(ctx: Stat): List<WInstruction> {
        TODO("Not yet implemented")
    }
}