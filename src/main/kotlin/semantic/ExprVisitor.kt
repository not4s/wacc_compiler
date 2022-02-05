package semantic

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
    val symbolTable: SymbolTable<IdentifierData> = EmptySymbolTable()
) : WACCParserBaseVisitor<Any?>() {

    private val typeMap: MutableMap<ParserRuleContext?, ExprType> = mutableMapOf()

    /**
     * Checks the matching of expected and actual values of ExprType.
     * Exits process via raiseTypeErrorAndExit() function if the types are not equal
     */
    private fun checkType(ctx: ParserRuleContext?, expectedType: ExprType) {
        Debug.infoLog("${ctx?.javaClass} '${ctx?.text}' of type ${typeMap[ctx]} provided, expected type is $expectedType")
        // TODO: Remove NOT_A_TYPE from acceptable types of ExprType
        if (typeMap[ctx] != expectedType && typeMap[ctx] != ExprType.NOT_A_TYPE) {
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
        val operandsExpectedType: ExprType = when(ctx.binOp.type) {
            OP_GT, OP_GEQ, OP_LT, OP_LEQ -> ExprType.INT
            else -> ExprType.BOOL
        }
        checkType(ctx.left, operandsExpectedType)
        checkType(ctx.right, operandsExpectedType)
        typeMap[ctx] = ExprType.BOOL
        return res
    }
    
    override fun visitExprCharUnary(ctx: ExprCharUnaryContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        checkType(ctx.operand, ExprType.INT)
        return res
    }
    
    override fun visitExprIntBinary(ctx: ExprIntBinaryContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        checkType(ctx.left, ExprType.INT)
        checkType(ctx.right, ExprType.INT)
        typeMap[ctx] = ExprType.INT
        return res
    }

    // TODO: Extract identifier type from Symbol Table
    override fun visitExprIdentifier(ctx: ExprIdentifierContext?): Any? {
        val res = visitChildren(ctx)
        ctx ?: return res
        val identifierType = symbolTable.get(ctx.IDENTIFIER().text)?.returnType
        typeMap[ctx] = identifierType ?: ExprType.INVALID_TYPE
        typeMap[ctx] = ExprType.NOT_A_TYPE
        return res
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

    /**
    * Default implementation of Symbol Table for ExprVisitor
    * @returns null and does nothing
    * */
    class EmptySymbolTable : SymbolTable<IdentifierData> {
        override fun get(symbol: String): IdentifierData? = null
        override fun declare(symbol: String, value: IdentifierData) {}
        override fun reassign(symbol: String, value: IdentifierData) {}
        override fun createChildScope(): SymbolTable<IdentifierData> = EmptySymbolTable()
    }
}