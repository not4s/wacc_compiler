package semantic

import SymbolTable.SymbolTable
import WACCType.*
import antlr.WACCParser.*
import antlr.WACCParserBaseVisitor
import org.antlr.v4.runtime.ParserRuleContext
import utils.Debug
import utils.ExitCode
import utils.raiseTypeErrorAndExit
import kotlin.system.exitProcess

enum class ExprType {
    INT,
    BOOL,
    CHAR,
    STRING,
    ARRAY_ANY,
    PAIR_ANY,
    NOT_A_TYPE,   // temporary
    INVALID_TYPE;
}

class ExprVisitor(
    private val symbolTable: SymbolTable
) : WACCParserBaseVisitor<Any?>() {

    private val typeMap: MutableMap<ParserRuleContext?, WAny> = mutableMapOf()

    /**
     * Checks the matching of expected and actual values of ExprType.
     * Exits process via raiseTypeErrorAndExit() function if the types are not equal
     */
    private fun checkType(ctx: ParserRuleContext?, expectedType: WAny) {
        Debug.infoLog("${ctx?.javaClass} '${ctx?.text}' of type ${typeMap[ctx]} provided, expected type is $expectedType")
        // TODO: Remove NOT_A_TYPE from acceptable types of ExprType
        if (typeMap[ctx] != expectedType) {
            raiseTypeErrorAndExit(ctx, expectedType, typeMap[ctx])
        }
    }

    /**
     * Checks that the integer is withing the 32-bit signed range
     * Sets the type of the context as Int
     */
    override fun visitLiteralInteger(ctx: LiteralIntegerContext?): Any? {
        try {
            Integer.parseInt(ctx?.text)
        } catch (e: java.lang.NumberFormatException) {
            exitProcess(ExitCode.SYNTAX_ERROR)
        }
        typeMap[ctx] = WInt()
        return visitChildren(ctx)
    }
    
    override fun visitLiteralBoolean(ctx: LiteralBooleanContext?): Any? {
        typeMap[ctx] = WBool()
        return visitChildren(ctx)
    }
    
    override fun visitLiteralChar(ctx: LiteralCharContext?): Any? {
        typeMap[ctx] = WChar()
        return visitChildren(ctx)
    }
    
    override fun visitLiteralString(ctx: LiteralStringContext?): Any? {
        typeMap[ctx] = WStr()
        return visitChildren(ctx)
    }
    
    override fun visitLiteralPair(ctx: LiteralPairContext?): Any? {
        typeMap[ctx] = WPair<WAny, WAny>()
        return visitChildren(ctx)
    }
    
    override fun visitExprBoolUnary(ctx: ExprBoolUnaryContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        checkType(ctx.operand, WBool())
        return res
    }
    
    override fun visitExprBracket(ctx: ExprBracketContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        typeMap[ctx] = typeMap[ctx.innerExpr]!!
        return res
    }

    override fun visitExprBoolBinary(ctx: ExprBoolBinaryContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        val operandsExpectedType: WAny = when(ctx.binOp.type) {
            OP_GT, OP_GEQ, OP_LT, OP_LEQ -> WInt()
            else -> WBool()
        }
        checkType(ctx.left, operandsExpectedType)
        checkType(ctx.right, operandsExpectedType)
        typeMap[ctx] = WBool()
        return res
    }
    
    override fun visitExprCharUnary(ctx: ExprCharUnaryContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        checkType(ctx.operand, WInt())
        return res
    }
    
    override fun visitExprIntBinary(ctx: ExprIntBinaryContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        checkType(ctx.left, WInt())
        checkType(ctx.right, WInt())
        typeMap[ctx] = WInt()
        return res
    }

    // TODO: Extract identifier type from Symbol Table
    override fun visitExprIdentifier(ctx: ExprIdentifierContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        val identifierType = symbolTable.get(ctx.IDENTIFIER().text)
        typeMap[ctx] = identifierType
        return res
    }
    
    override fun visitExprLiteral(ctx: ExprLiteralContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        typeMap[ctx] = typeMap[ctx.literal()]!!
        return res
    }
    
    override fun visitExprIntUnary(ctx: ExprIntUnaryContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        when (ctx.unOp.type) {
            OP_ORD -> checkType(ctx.operand, WChar())
            OP_LEN -> checkType(ctx.operand, WArray<WAny>()) // TODO: Can we find len of strings?
            OP_SUBT -> checkType(ctx.operand, WInt())
        }
        typeMap[ctx] = WInt()
        return res
    }
}