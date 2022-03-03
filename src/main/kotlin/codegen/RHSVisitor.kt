package codegen

import ast.ArrayLiteral
import ast.Expr
import ast.Literal
import ast.RHS
import ast.NewPairRHS
import instructions.WInstruction
import instructions.misc.*
import instructions.operations.*
import utils.btoi
import waccType.*
import kotlin.collections.listOf

// Stores visiting result in Register.resultRegister.
class RHSVisitor(val data: DataDeclaration, val rp : RegisterProvider, val funcPool: FunctionPool) : ASTVisitor<RHS> {

    private val registerProvider = RegisterProvider()

    // Stores result of visiting in R4.
    override fun visit(ctx: RHS): List<WInstruction> {
        return when (ctx) {
            is Literal -> visitLiteral(ctx)
            is ArrayLiteral -> visitArrayLiteral(ctx)
            is Expr -> ExprVisitor(data, rp, funcPool).visit(ctx)
            is NewPairRHS -> visitNewPair(ctx)
            else -> TODO("Not yet implemented")
        }
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
        val mallocResReg = registerProvider.get()
        val pairValueStoreReg = registerProvider.get()
        
        return listOf<WInstruction>(B(("malloc"), true)).
            // assembly for the first new element
            plus(MOV(mallocResReg, Register.resultRegister())).
            plus(visitElem(ctx.left.type, pairValueStoreReg, 0)).    
            plus(B("malloc", true)).
            plus(STR(pairValueStoreReg, Register.resultRegister(), 0,
                 ctx.left.type is WBool || ctx.right.type is WChar)).
            plus(STR(Register.resultRegister(), mallocResReg)).
            
            // assembly for the second new element
            plus(visitElem(ctx.right.type, pairValueStoreReg, WORD_SIZE)).
            plus(B("malloc", true)).
            plus(STR(pairValueStoreReg, Register.resultRegister(), 0,
                     ctx.left.type is WBool || ctx.right.type is WChar)).
            plus(STR(Register.resultRegister(), mallocResReg, WORD_SIZE)).
            plus(STR(mallocResReg, Register.stackPointer()))
    }

    private fun visitElem(
        ctx: WAny,
        pairValueStoreReg: Register,
        offset: Int
    ): List<WInstruction> {
        when(ctx) {
            is WInt -> {
                return listOf<WInstruction>(
                    LDR(pairValueStoreReg, LoadImmediate(ctx.value!!)),
                    LDR(Register.resultRegister(), LoadImmediate(WORD_SIZE))
                )
            }
            is WStr -> {
                DataDeclaration().addDeclaration(ctx.value!!)
                return listOf<WInstruction>(
                    LDR(pairValueStoreReg, LabelReference(ctx.value!!)),
                    LDR(Register.resultRegister(), LoadImmediate(WORD_SIZE))
                )
            }
            is WBool -> {
                val intVal: Int
                if(ctx.value!!) intVal = 1 else intVal = 0 
                return listOf<WInstruction>(
                    MOV(pairValueStoreReg, Immediate(intVal)),
                    LDR(Register.resultRegister(), LoadImmediate(1))
                )
            }
            is WChar -> {
                return listOf<WInstruction>(
                    MOV(pairValueStoreReg, ImmediateChar(ctx.value!!)),
                    LDR(Register.resultRegister(), LoadImmediate(WORD_SIZE))
                )
            }
            is WPair -> {
                return listOf<WInstruction>(
                    LDR(pairValueStoreReg, ImmediateOffset(Register.stackPointer(), offset)),
                    LDR(Register.resultRegister(), LoadImmediate(WORD_SIZE))
                )
            }
            else -> throw error("Unreachable")
        }
    }
}
