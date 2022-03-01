package codegen

import ast.AST
import instructions.WInstruction
import instructions.misc.DataDeclaration

interface ASTVisitor<T : AST> {
    fun visit(ctx: T): List<WInstruction>
}