package codegen

import ast.*
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*
import utils.btoi
import waccType.*

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
        val evalCodes: List<WInstruction> = ctx.params.map {
            val exprVisitor = ExprVisitor(data, registerProvider, funcPool)
            val evalCode = exprVisitor.visit(it)
            val storeInstr = PUSH(Register.resultRegister(), null)
            evalCode.plus(storeInstr)
        }.flatten()
        return evalCodes.plus(
            listOf(
                B(funcLabel(ctx.identifier), link = true),
                ADD(Register.stackPointer(), Register.stackPointer(), Immediate(ctx.params.size * 4))
            )
        )
    }

    private fun visitArrayLiteral(ctx: ArrayLiteral): List<WInstruction> {
        val arrSize = ctx.values.size
        val mallocResReg = registerProvider.get()
        val arrValueStoreReg = registerProvider.get()
        var index = 0
        return listOf(
            LDR(Register.resultRegister(), LoadImmediate(arrSize + WORD_SIZE)),
            B(MALLOC, link = true),
            MOV(mallocResReg, Register.resultRegister())
        ).plus(
            ctx.values.map {
                listOf(
                    MOV(arrValueStoreReg, immediateOfCorrectType(it)),
                    STR(arrValueStoreReg, mallocResReg, (index++) + WORD_SIZE)
                )
            }.reduce(List<WInstruction>::plus)
        ).plus(
            listOf(
                LDR(arrValueStoreReg, LoadImmediate(arrSize)),
                STR(arrValueStoreReg, mallocResReg),
                STR(mallocResReg, Register.stackPointer())
            )
        )
    }

    private fun immediateOfCorrectType(expr: Expr): Operand2 {
        return when (val type = expr.type) {
            is WInt -> Immediate(type.value ?: throw Exception("Couldn't get literal int"))
            is WChar -> ImmediateChar(type.value ?: throw Exception("Couldn't get literal char"))
            is WBool -> Immediate(btoi(type.value ?: throw Exception("Couldn't get literal bool")))
            else -> throw Exception("Unknown immediate of ${expr.type}")
        }
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
        val mallocResReg = rp.get()
        val pairValueStoreReg = rp.get()

        val instr = listOf<WInstruction>(
            // allocate memory for both the elements
            LDR(Register.resultRegister(), LoadImmediate(PAIR_SIZE * 2))
        )
            .plus(B((MALLOC), true))
            .plus(MOV(mallocResReg, Register.resultRegister()))
            .plus(visitElem(ctx.left.type, pairValueStoreReg, 0))
            .plus(B(MALLOC, true))
            .plus(
                STR(
                    pairValueStoreReg, Register.resultRegister(), 0,
                    ctx.type.leftType is WBool || ctx.type.leftType is WChar
                )
            ).plus(STR(Register.resultRegister(), mallocResReg))
            .plus(visitElem(ctx.right.type, pairValueStoreReg, PAIR_SIZE))
            .plus(B(MALLOC, true))
            .plus(
                STR(
                    pairValueStoreReg, Register.resultRegister(), 0,
                    ctx.type.rightType is WBool || ctx.type.rightType is WChar
                )
            ).plus(STR(Register.resultRegister(), mallocResReg, PAIR_SIZE))
            .plus(
                when (lhs) {
                    is IdentifierSet -> ctx.st.asmAssign(lhs.identifier, Register(4), data, null)
                    else -> listOf()
                }
            )
            .plus(
                when (lhs) {
                    is IdentifierSet -> ctx.st.asmGet(lhs.identifier, Register(4), data)
                    else -> listOf()
                }
            )

        rp.ret()
        rp.ret()

        return instr
    }

    private fun visitElem(
        ctx: WAny,
        pairValueStoreReg: Register,
        offset: Int
    ): List<WInstruction> {
        when (ctx) {
            is WInt -> {
                return listOf<WInstruction>(
                    LDR(pairValueStoreReg, LoadImmediate(ctx.value!!)),
                    // Load the size of Int from the heap
                    LDR(Register.resultRegister(), LoadImmediate(INT_SIZE))
                )
            }
            is WStr -> {
                DataDeclaration().addDeclaration(ctx.value!!)
                return listOf<WInstruction>(
                    LDR(pairValueStoreReg, LabelReference(ctx.value)),
                    // Load the size of Str from the heap
                    LDR(Register.resultRegister(), LoadImmediate(STR_SIZE))
                )
            }
            is WBool -> {
                return listOf(
                    MOV(pairValueStoreReg, Immediate(btoi(ctx.value!!))),
                    // Load the size of Bool from the heap
                    LDR(Register.resultRegister(), LoadImmediate(BOOL_SIZE))
                )
            }
            is WChar -> {
                return listOf(
                    MOV(pairValueStoreReg, ImmediateChar(ctx.value!!)),
                    // Load the size of Char from the heap
                    LDR(Register.resultRegister(), LoadImmediate(CHAR_SIZE))
                )
            }
            is WPair -> {
                return listOf<WInstruction>(
                    LDR(pairValueStoreReg, ImmediateOffset(Register.stackPointer(), offset)),
                    // Load the size of Pair from the heap
                    LDR(Register.resultRegister(), LoadImmediate(PAIR_SIZE))
                )
            }
            is WPairNull -> {
                return listOf(
                    LDR(pairValueStoreReg, LoadImmediate(0)),
                    LDR(Register.resultRegister(), LoadImmediate(WORD_SIZE))
                )
            }
            else -> throw Exception("Unreachable ${ctx::class}")
        }
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
        val instr = listOf(Label("# jopa"))
            .plus(visit(ctx.expr))
            .plus(listOf(Label("# jopa2")))
            .plus(B(CHECK_NULL_POINTER, link = true))
            .plus(LDR(Register.resultRegister(), ImmediateOffset(Register.resultRegister(), offset), isSignedByte = charOrBool))
        return instr
    }
}
