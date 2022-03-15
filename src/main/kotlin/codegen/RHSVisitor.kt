package codegen

import ast.*
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*
import symbolTable.typeToByteSize
import waccType.*

// Stores visiting result in Register.R0
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
            is WACCStruct -> visitStruct(ctx)
        }
    }

    private fun visitStruct(ctx: WACCStruct): List<WInstruction> {
        // calculate size of struct
        val sizeOfStruct: Int = ctx.elements.entries.sumOf { (_, type) -> typeToByteSize(type) }
        // declaring a struct and storing the address into the stack
        return listOf(LDR(Register.R0, LoadImmediate(sizeOfStruct)), B(MALLOC))
    }

    private fun visitFunctionCall(ctx: FunctionCall): List<WInstruction> {
        var totalOffset = 0
        val evalCodes: List<WInstruction> = ctx.params.map {
            val exprVisitor = ExprVisitor(data, registerProvider, funcPool)
            val evalCode = exprVisitor.visit(it)
            val byteSize = typeToByteSize(it.type)
            val storeInstr =
                STR(
                    Register.R0,
                    Register.SP,
                    offset = -(totalOffset + byteSize),
                    isSignedByte = byteSize != WORD_SIZE,
                )
            totalOffset += byteSize
            evalCode.plus(storeInstr)
        }.flatten()
        return evalCodes.plus(
            listOf(
                SUB(Register.SP, Register.SP, Immediate(totalOffset)),
                B(funcLabel(ctx.identifier)),
                ADD(Register.SP, Register.SP, Immediate(totalOffset))
            )
        )
    }

    private fun visitArrayLiteral(ctx: ArrayLiteral): List<WInstruction> {
        val arrSize = ctx.values.size
        val mallocResReg = registerProvider.get()
        val arrValueStoreReg = Register.R0
        var index = 0
        return listOf(
            LDR(
                Register.R0,
                LoadImmediate(WORD_SIZE + arrSize * typeToByteSize(ctx.type.elemType))
            ),
            B(MALLOC),
            MOV(mallocResReg, Register.R0)
        ).plus(
            ctx.values.map {
                ExprVisitor(data, registerProvider, funcPool).visit(it).plus(
                    STR(
                        arrValueStoreReg,
                        mallocResReg,
                        WORD_SIZE + (index++) * typeToByteSize(ctx.type.elemType)
                    )
                )
            }.fold(listOf()) { a, b -> a.plus(b) }
        ).plus(
            listOf(
                LDR(arrValueStoreReg, LoadImmediate(arrSize)),
                STR(arrValueStoreReg, mallocResReg),
                MOV(Register.R0, mallocResReg)
            )
        )
    }

    private fun visitLiteral(ctx: Literal): List<WInstruction> {
        return when (ctx.type) {
            is WInt -> listOf(LDR(Register.R0, LoadImmediate(ctx.type.value!!)))
            is WBool -> listOf(
                LDR(
                    Register.R0, LoadImmediate(
                        if (ctx.type.value!!) {
                            1
                        } else 0
                    )
                )
            )
            is WChar -> listOf(MOV(Register.R0, ImmediateChar(ctx.type.value!!)))
            is WStr -> {
                val reference = data.addDeclaration(ctx.type.value!!)
                listOf(
                    LDR(Register.R0, LabelReference(reference))
                )
            }
            else -> TODO("Unimplemented")
        }
    }

    private fun visitNewPair(ctx: NewPairRHS): List<WInstruction> {

        return listOf<WInstruction>().plus(
            // Visit first element
            ExprVisitor(data, rp, funcPool).visit(ctx.left)
        ).plus(
            listOf(
                // Move it t.R1               
                MOV(Register.R9, Register.R0),
                // Malloc space
                LDR(Register.R0, LoadImmediate(typeToByteSize(ctx.left.type))),
                B(MALLOC),
                // STR r1 to r0
                STR(
                    Register.R9,
                    Register.R0,
                    isSignedByte = typeToByteSize(ctx.left.type) == 1
                ),
                PUSH(Register.R0, data)
            )
        ).plus(
            // Visit first element
            ExprVisitor(data, rp, funcPool).visit(ctx.right)
        ).plus(
            listOf(
                // Move it t.R1               
                MOV(Register.R9, Register.R0),
                // Malloc space
                LDR(Register.R0, LoadImmediate(typeToByteSize(ctx.right.type))),
                B(MALLOC),
                // STR r1 to r0
                STR(
                    Register.R9,
                    Register.R0,
                    isSignedByte = typeToByteSize(ctx.right.type) == 1
                ),
                PUSH(Register.R0, data)
            )
        ).plus(
            // Now stack has first, second pointers.
            // Malloc space for pair
            LDR(Register.R0, LoadImmediate(8))
        ).plus(
            // Convert to ptr
            B(MALLOC)
        ).plus(
            listOf(
                // Pop SECOND element, store.
                POP(Register.R1, data),
                STR(Register.R1, Register.R0, offset = 4)
            )
        ).plus(
            listOf(
                // Pop FIRST element, store.
                POP(Register.R1, data),
                STR(Register.R1, Register.R0)
            )
        )
    }


    private fun visitPairLiteral(): List<WInstruction> {
        return listOf(
            LDR(Register.R0, LoadImmediate(0))
        )
    }

    private fun visitPairElement(ctx: PairElement): List<WInstruction> {
        val offset = if (ctx.first) 0 else WORD_SIZE
        val charOrBool = (lhs?.type is WChar || lhs?.type is WBool)

        data.addDeclaration(NULL_POINTER_MESSAGE)
        pCheckNullPointer(data, funcPool)
        val instr = listOf<WInstruction>()
            .plus(visit(ctx.expr))
            .plus(B(CHECK_NULL_POINTER))
            .plus(
                LDR(
                    Register.R0,
                    ImmediateOffset(Register.R0, offset)
                )
            )
            .plus(
                LDR(
                    Register.R0,
                    ImmediateOffset(Register.R0),
                    isSignedByte = charOrBool
                )
            )
        return instr
    }
}
