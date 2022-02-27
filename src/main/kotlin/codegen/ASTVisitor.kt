package codegen

import ast.AST
import instructions.WInstruction

interface ASTVisitor<T : AST> {
    fun visit(ctx: T): List<WInstruction>
}