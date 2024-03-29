package codegen

import ast.AST
import ast.Stat
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
            SUB(Register.SP, Register.SP, Immediate(offset % 1024))
        )
        for (i in 0 until offset / 1024) {
            output = output.plus(
                SUB(Register.SP, Register.SP, Immediate(1024))
            )
        }
        return output
    }

    fun unOffsetStackBy(offset: Int): List<WInstruction> {
        var output = listOf(
            ADD(Register.SP, Register.SP, Immediate(offset % 1024))
        )
        for (i in 0 until offset / 1024) {
            output = output.plus(
                ADD(Register.SP, Register.SP, Immediate(1024))
            )
        }
        return output
    }

    fun withScope(offset: Int, generator: List<WInstruction>): List<WInstruction> {
        return offsetStackBy(offset).plus(generator).plus(unOffsetStackBy(offset))
    }
}