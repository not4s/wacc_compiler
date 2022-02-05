package semantic

import antlr.WACCParser.*
import antlr.WACCParserBaseVisitor
import org.antlr.v4.runtime.ParserRuleContext
import utils.raiseTypeErrorAndExit

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

class ExprVisitor() : WACCParserBaseVisitor<Any?>() {

    private val typeMap: MutableMap<ParserRuleContext?, ExprType> = mutableMapOf()

    private fun checkType(ctx: ParserRuleContext?, expectedType: ExprType) {
        if (typeMap[ctx] != ExprType.BOOL && typeMap[ctx] != ExprType.NOT_A_TYPE) {
            raiseTypeErrorAndExit(ctx, expectedType, typeMap[ctx])
        }
    }

    override fun visitLiteralInteger(ctx: LiteralIntegerContext?): Any? {
        typeMap[ctx] = ExprType.INT
        return visitChildren(ctx)
    }
    
    override fun visitLiteralBoolean(ctx: LiteralBooleanContext?): Any? {
        typeMap[ctx] = ExprType.BOOL
        return visitChildren(ctx)
    }
    
    override fun visitLiteralChar(ctx: LiteralCharContext?): Any? {
        typeMap[ctx] = ExprType.CHAR
        return visitChildren(ctx)
    }
    
    override fun visitLiteralString(ctx: LiteralStringContext?): Any? {
        typeMap[ctx] = ExprType.STRING
        return visitChildren(ctx)
    }
    
    override fun visitLiteralPair(ctx: LiteralPairContext?): Any? {
        typeMap[ctx] = ExprType.PAIR_ANY
        return visitChildren(ctx)
    }
    
    override fun visitExprBoolUnary(ctx: ExprBoolUnaryContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        checkType(ctx.operand, ExprType.BOOL)
        return res
    }
    
    override fun visitExprBracket(ctx: ExprBracketContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        typeMap[ctx] = typeMap[ctx.innerExpr] ?: ExprType.INVALID_TYPE
        return res
    }

    override fun visitExprBoolBinary(ctx: ExprBoolBinaryContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        checkType(ctx.left, ExprType.BOOL)
        checkType(ctx.left, ExprType.BOOL)
        return res
    }
    
    override fun visitExprCharUnary(ctx: ExprCharUnaryContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        checkType(ctx.operand, ExprType.INT)
        return res
    }
    
    override fun visitExprIntBinary(ctx: ExprIntBinaryContext?): Any? {
        typeMap[ctx] = ExprType.NOT_A_TYPE
        return visitChildren(ctx)
    }

    // TODO: Extract identifier type from Symbol Table
    override fun visitExprIdentifier(ctx: ExprIdentifierContext?): Any? {
//        typeMap[ctx] = typeMap[exprType(ctx.IDENTIFIER())] ?: ExprType.NOT_A_TYPE
        typeMap[ctx] = ExprType.NOT_A_TYPE
        return visitChildren(ctx)
    }
    
    override fun visitExprLiteral(ctx: ExprLiteralContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        typeMap[ctx] = typeMap[ctx.literal()] ?: ExprType.INVALID_TYPE
        return res
    }
    
    override fun visitExprIntUnary(ctx: ExprIntUnaryContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        when (ctx.unOp.type) {
            OP_ORD -> checkType(ctx.operand, ExprType.CHAR)
            OP_LEN -> checkType(ctx.operand, ExprType.ARRAY_ANY) // TODO: Can we find len of strings?
            OP_SUBT -> checkType(ctx.operand, ExprType.INT)
        }
        typeMap[ctx] = ExprType.INT
        return res
    }
}