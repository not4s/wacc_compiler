package codegen

import ast.AST
import instructions.WInstruction
import instructions.aux.DataDeclaration

interface ASTVisitor<T : AST> {
    fun visit(ctx: T, data: DataDeclaration): List<WInstruction>
}