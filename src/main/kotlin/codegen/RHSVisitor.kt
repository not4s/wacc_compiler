package codegen

import ast.*
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*
import symbolTable.typeToByteSize
import waccType.WBool
import waccType.WChar
import waccType.WInt
import waccType.WStr

// Stores visiting result in Register.resultRegister.
class RHSVisitor(
    val data: DataDeclaration,
    val rp: RegisterProvider,
    val funcPool: FunctionPool,
    val lhs: LHS? = null
) : ASTVisitor<RHS> {

    private val registerProvider = RegisterProvider()

    // Stores result of visiting in R4.
    override fun visit(ctx: RHS): List<WInstruction> {
        return when (ctx) {
            is Literal -> visitLiteral(ctx)
            is ArrayLiteral -> visitArrayLiteral(ctx)
            is NewPairRHS -> visitNewPair(ctx)
            is PairLiteral -> visitPairLiteral()
            is PairElement -> visitPairElement(ctx)
            is Expr -> ExprVisitor(data, rp, funcPool).visit(ctx)
            is FunctionCall -> visitFunctionCall(ctx)
            else -> TODO("Not yet implemented")
        }
    }

    private fun visitFunctionCall(ctx: FunctionCall): List<WInstruction> {
        var totalOffset = 0
        val evalCodes: List<WInstruction> = ctx.params.map {
            val exprVisitor = ExprVisitor(data, registerProvider, funcPool)
            val evalCode = exprVisitor.visit(it)
            val byteSize = typeToByteSize(it.type)
            val storeInstr =
                STR(
                    Register.resultRegister(),
                    Register.stackPointer(),
                    offset = -(totalOffset + byteSize),
                    isSignedByte = byteSize != WORD_SIZE,
                )
            totalOffset += byteSize
            evalCode.plus(storeInstr)
        }.flatten()
        return evalCodes.plus(
            listOf(
                SUB(Register.stackPointer(), Register.stackPointer(), Immediate(totalOffset)),
                B(funcLabel(ctx.identifier), link = true),
                ADD(Register.stackPointer(), Register.stackPointer(), Immediate(totalOffset))
            )
        )
    }

    private fun visitArrayLiteral(ctx: ArrayLiteral): List<WInstruction> {
        val arrSize = ctx.values.size
        val mallocResReg = registerProvider.get()
        val arrValueStoreReg = Register.resultRegister()
        var index = 0
        return listOf(
            LDR(Register.resultRegister(), LoadImmediate(WORD_SIZE + arrSize * typeToByteSize(ctx.type.elemType))),
            B(MALLOC, link = true),
            MOV(mallocResReg, Register.resultRegister())
        ).plus(
            ctx.values.map {
                ExprVisitor(data, registerProvider, funcPool).visit(it).plus(
                    STR(arrValueStoreReg, mallocResReg, WORD_SIZE + (index++) * typeToByteSize(ctx.type.elemType))
                )
            }.fold(listOf()) { a, b -> a.plus(b) }
        ).plus(
            listOf(
                LDR(arrValueStoreReg, LoadImmediate(arrSize)),
                STR(arrValueStoreReg, mallocResReg),
                MOV(Register.resultRegister(), mallocResReg)
            )
        )
    }

    private fun visitLiteral(ctx: Literal): List<WInstruction> {
        return when (ctx.type) {
            is WInt -> listOf(LDR(Register.resultRegister(), LoadImmediate(ctx.type.value!!)))
            is WBool -> listOf(
                LDR(
                    Register.resultRegister(), LoadImmediate(
                        if (ctx.type.value!!) {
                            1
                        } else 0
                    )
                )
            )
            is WChar -> listOf(MOV(Register.resultRegister(), ImmediateChar(ctx.type.value!!)))
            is WStr -> {
                val reference = data.addDeclaration(ctx.type.value!!)
                listOf(
                    LDR(Register.resultRegister(), LabelReference(reference))
                )
            }
            else -> TODO("Not yet implemented")
        }
    }

    private fun visitNewPair(ctx: NewPairRHS): List<WInstruction> {

        return listOf<WInstruction>().plus(
            // Visit first element
            ExprVisitor(data, rp, funcPool).visit(ctx.left)
        ).plus(
            listOf(
                // Move it to r1
                MOV(Register("r9"), Register.resultRegister()),
                // Malloc space
                LDR(Register.resultRegister(), LoadImmediate(typeToByteSize(ctx.left.type))),
                B(MALLOC, link = true),
                // STR r1 to r0
                STR(Register("r9"), Register.resultRegister(), isSignedByte = typeToByteSize(ctx.left.type) == 1),
                PUSH(Register.resultRegister(), data)
            )
        ).plus(
            // Visit first element
            ExprVisitor(data, rp, funcPool).visit(ctx.right)
        ).plus(
            listOf(
                // Move it to r1
                MOV(Register("r9"), Register.resultRegister()),
                // Malloc space
                LDR(Register.resultRegister(), LoadImmediate(typeToByteSize(ctx.right.type))),
                B(MALLOC, link = true),
                // STR r1 to r0
                STR(Register("r9"), Register.resultRegister(), isSignedByte = typeToByteSize(ctx.right.type) == 1),
                PUSH(Register.resultRegister(), data)
            )
        ).plus(
            // Now stack has first, second pointers.
            // Malloc space for pair
            LDR(Register.resultRegister(), LoadImmediate(8))
        ).plus(
            // Convert to ptr
            B(MALLOC, link = true)
        ).plus(
            listOf(
                // Pop SECOND element, store.
                POP(Register("r1"), data),
                STR(Register("r1"), Register.resultRegister(), offset = 4)
            )
        ).plus(
            listOf(
                // Pop FIRST element, store.
                POP(Register("r1"), data),
                STR(Register("r1"), Register.resultRegister())
            )
        )
    }


    private fun visitPairLiteral(): List<WInstruction> {
        return listOf(
            LDR(Register.resultRegister(), LoadImmediate(0))
        )
    }

    private fun visitPairElement(ctx: PairElement): List<WInstruction> {
        val offset = if (ctx.first) 0 else WORD_SIZE
//        var charOrBool = false
//        if (lhs is IdentifierSet) {
        val charOrBool = (lhs?.type is WChar || lhs?.type is WBool)
//        }
        data.addDeclaration(NULL_POINTER_MESSAGE)
        pCheckNullPointer(data, funcPool)
        val instr = listOf<WInstruction>()
            .plus(visit(ctx.expr))
            .plus(B(CHECK_NULL_POINTER, link = true))
            .plus(LDR(Register.resultRegister(), ImmediateOffset(Register.resultRegister(), offset)))
            .plus(LDR(Register.resultRegister(), ImmediateOffset(Register.resultRegister()), isSignedByte = charOrBool))
        return instr
    }
}
