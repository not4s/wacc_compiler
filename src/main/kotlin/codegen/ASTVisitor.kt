package codegen

import ast.AST
import instructions.WInstruction
import instructions.misc.Immediate
import instructions.misc.Register
import instructions.operations.ADD
import instructions.operations.SUB

interface ASTVisitor<T : AST> {
    fun visit(ctx: T): List<WInstruction>

    // Needed since can only do #1024 offset maximum
    fun offsetStackBy(offset: Int): List<WInstruction> {
        var output = listOf(
            SUB(Register.stackPointer(), Register.stackPointer(), Immediate(offset % 1024))
        )
        for (i in 0 until offset / 1024) {
            output = output.plus(
                SUB(Register.stackPointer(), Register.stackPointer(), Immediate(1024))
            )
        }
        return output
    }

    fun unOffsetStackBy(offset: Int): List<WInstruction> {
        var output = listOf(
            ADD(Register.stackPointer(), Register.stackPointer(), Immediate(offset % 1024))
        )
        for (i in 0 until offset / 1024) {
            output = output.plus(
                ADD(Register.stackPointer(), Register.stackPointer(), Immediate(1024))
            )
        }
        return output
    }
}