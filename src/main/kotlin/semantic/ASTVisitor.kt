package semantic

import antlr.WACCParser
import antlr.WACCParserBaseVisitor
import ast.*
import ast.statement.*
import symbolTable.SymbolTable
import utils.*
import waccType.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

class ASTVisitor(
    private val st: SymbolTable,
) : WACCParserBaseVisitor<AST>() {

    private var semanticErrorCount: AtomicInteger = AtomicInteger(0)

    /**
     * Wrapper around try-catch block to write less repetitive code
     * @param default is the AST which is returned if an error occured
     * @param block is the action to perform inside 'try' clause
     * @return The resultant AST
     */
    private fun safeVisit(default: AST, block: () -> AST): AST {
        return try {
            block.invoke()
        } catch (e: SemanticException) {
            semanticErrorCount.incrementAndGet()
            default
        }
    }

    /**
     * Used for transferring the Boolean value of the fact of semantic error occurrence
     */
    private constructor(st: SymbolTable, errorAlreadyOccurred: AtomicInteger) : this(st) {
        semanticErrorCount = errorAlreadyOccurred
    }

    override fun visitProgram(ctx: WACCParser.ProgramContext): Stat {
        // This adds functions to symbol table
        val waccFunctions: MutableList<Pair<WACCParser.FuncContext, WACCFunction>> = mutableListOf()
        for (f in ctx.func()) {
            val errBuilder = SemanticErrorMessageBuilder()
                .provideStart(PositionedError(f))
                .setLineTextFromSrcFile(st.srcFilePath)
            val id = f.IDENTIFIER().text
            if (id in st.getMap()) {
                errBuilder.functionRedefineError(id)
                    .buildAndPrint()
                semanticErrorCount.incrementAndGet()
            }
            val bodyLessFunction = visitFuncParams(f)
            try {
                st.declare(
                    symbol = id,
                    value = bodyLessFunction,
                    errBuilder = builderTemplateFromContext(ctx, st)
                )
            } catch (e: SemanticException) {
                semanticErrorCount.incrementAndGet()
            }
            waccFunctions.add(Pair(f, bodyLessFunction))
        }
        for ((funCtx, waccFun) in waccFunctions) {
            val funcAST = safeVisit(waccFun) { visitFuncBody(waccFun, funCtx) } as WACCFunction
            st.reassign(
                funcAST.identifier,
                funcAST,
                builderTemplateFromContext(funCtx, st)
            )
        }
        // Explicitly call checks after defining all functions
        for ((_, f) in st.getMap()) {
            f as WACCFunction
            try {
                f.check()
            } catch (e: SemanticException) {
                semanticErrorCount.incrementAndGet()
            }
        }

        // Create a child scope, functions are now stored in parent table.
        // This scope is still 'global'
        val childScope = st.createChildScope()
        childScope.isGlobal = true
        val programAST =
            safeVisit(SkipStat(st)) { ASTVisitor(childScope, semanticErrorCount).visit(ctx.stat()) } as Stat
        val totalSemanticErrors = semanticErrorCount.get()
        if (totalSemanticErrors > 0) {
            throw SemanticException("Semantic errors detected: $totalSemanticErrors, compilation aborted.")
        }
        return programAST
    }

    override fun visitTypeBaseType(ctx: WACCParser.TypeBaseTypeContext): WACCType {
        return this.visit(ctx.baseType()) as WACCType
    }

    override fun visitTypeArrayType(ctx: WACCParser.TypeArrayTypeContext): WACCType {
        return this.visit(ctx.arrayType())  as WACCType
    }

    override fun visitTypePairType(ctx: WACCParser.TypePairTypeContext): WACCType {
        return  this.visit(ctx.pairType())  as WACCType
    }

    // <something[]>[]
    override fun visitArrayTypeArrayType(ctx: WACCParser.ArrayTypeArrayTypeContext): WACCType {
        val elemType: WACCType = safeVisit(WACCType(st, WUnknown())) { this.visit(ctx.arrayType()) } as WACCType
        return WACCType(st, WArray(elemType.type))
    }

    // int[], str[], char[], bool[]
    override fun visitArrayTypeBaseType(ctx: WACCParser.ArrayTypeBaseTypeContext): WACCType {
        val elemType: WACCType = safeVisit(WACCType(st, WUnknown())) { this.visit(ctx.baseType()) } as WACCType
        return WACCType(st, WArray(elemType.type))
    }

    // pair[]
    override fun visitArrayTypePairType(ctx: WACCParser.ArrayTypePairTypeContext): AST {
        val elemType: WACCType = safeVisit(WACCType(st, WUnknown())) { this.visit(ctx.pairType()) } as WACCType
        return WACCType(st, WArray(elemType.type))
    }

    override fun visitArrayElem(ctx: WACCParser.ArrayElemContext): ArrayElement {
        val indices: Array<Expr> =
            ctx.expr().map { e -> safeVisit(Literal(st, WUnknown())) { this.visit(e) } as Expr }.toTypedArray()
        return ArrayElement(st, ctx.IDENTIFIER().text, indices, ctx)
    }

    override fun visitArrayLiterAssignRhs(ctx: WACCParser.ArrayLiterAssignRhsContext): ArrayLiteral {
        val elements: Array<WAny> =
            ctx.expr().map { e -> (safeVisit(Literal(st, WUnknown())) { this.visit(e) } as Expr).type }.toTypedArray()
        return ArrayLiteral(st, elements, ctx)
    }

    override fun visitPairElemFst(ctx: WACCParser.PairElemFstContext): AST {
        val expr = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.expr()) } as Expr
        return PairElement(st, true, expr, ctx)
    }

    override fun visitPairElemSnd(ctx: WACCParser.PairElemSndContext): AST {
        val expr = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.expr()) } as Expr
        return PairElement(st, false, expr, ctx)
    }

    override fun visitPairType(ctx: WACCParser.PairTypeContext): WACCType {
        val left = safeVisit(WACCType(st, WUnknown())) { this.visit(ctx.left) } as WACCType
        val right = safeVisit(WACCType(st, WUnknown())) { this.visit(ctx.right) } as WACCType
        return WACCType(st, WPair(left.type, right.type))
    }

    override fun visitPairElemTypeBaseType(ctx: WACCParser.PairElemTypeBaseTypeContext): WACCType {
        return safeVisit(WACCType(st, WUnknown())) { this.visit(ctx.baseType()) } as WACCType
    }

    override fun visitPairElemTypeArrayType(ctx: WACCParser.PairElemTypeArrayTypeContext): WACCType {
        return safeVisit(WACCType(st, WUnknown())) { this.visit(ctx.arrayType()) } as WACCType
    }

    override fun visitPairElemTypeKwPair(ctx: WACCParser.PairElemTypeKwPairContext): WACCType {
        return WACCType(st, WPairKW())
    }

    override fun visitBaseTypeInt(ctx: WACCParser.BaseTypeIntContext): WACCType {
        return WACCType(st, WInt())
    }

    override fun visitBaseTypeBool(ctx: WACCParser.BaseTypeBoolContext): WACCType {
        return WACCType(st, WBool())
    }

    override fun visitBaseTypeChar(ctx: WACCParser.BaseTypeCharContext): WACCType {
        return WACCType(st, WChar())
    }

    override fun visitBaseTypeString(ctx: WACCParser.BaseTypeStringContext): WACCType {
        return WACCType(st, WStr())
    }

    /**
     * Ensures that the integer literal is within the bounds (-2^32, 2^32 - 1)
     */
    override fun visitLiteralInteger(ctx: WACCParser.LiteralIntegerContext): Literal {
        try {
            Integer.parseInt(ctx.text)
        } catch (e: java.lang.NumberFormatException) {
            SyntaxErrorMessageBuilder()
                .provideStart(PositionedError(ctx))
                .setLineTextFromSrcFile(st.srcFilePath)
                .appendCustomErrorMessage("Attempted to parse a very big int ${ctx.text}!")
                .buildAndPrint()
            exitProcess(ExitCode.SYNTAX_ERROR)
        }
        return Literal(st, WInt())
    }

    override fun visitLiteralBoolean(ctx: WACCParser.LiteralBooleanContext): Literal {
        return Literal(st, WBool())
    }

    override fun visitLiteralChar(ctx: WACCParser.LiteralCharContext): Literal {
        return Literal(st, WChar())
    }

    override fun visitLiteralString(ctx: WACCParser.LiteralStringContext): Literal {
        return Literal(st, WStr())
    }

    override fun visitLiteralPair(ctx: WACCParser.LiteralPairContext): PairLiteral {
        return PairLiteral(st, WPairNull())
    }

    override fun visitExprBracket(ctx: WACCParser.ExprBracketContext): Expr {
        return safeVisit(Literal(st, WUnknown())) { this.visit(ctx.expr()) } as Expr
    }

    override fun visitExprArrayElem(ctx: WACCParser.ExprArrayElemContext): ArrayElement {
        return safeVisit(Literal(st, WUnknown())) { this.visit(ctx.arrayElem()) } as ArrayElement
    }

    override fun visitExprBinary(ctx: WACCParser.ExprBinaryContext): BinaryOperation {
        return BinaryOperation(
            st,
            safeVisit(Literal(st, WUnknown())) { this.visit(ctx.left) } as Expr,
            safeVisit(Literal(st, WUnknown())) { this.visit(ctx.right) } as Expr,
            op = when (ctx.binOp.type) {
                WACCParser.OP_MULT -> BinOperator.MUL
                WACCParser.OP_DIV -> BinOperator.DIV
                WACCParser.OP_MOD -> BinOperator.MOD
                WACCParser.OP_ADD -> BinOperator.ADD
                WACCParser.OP_SUBT -> BinOperator.SUB
                WACCParser.OP_GT -> BinOperator.GT
                WACCParser.OP_GEQ -> BinOperator.GEQ
                WACCParser.OP_LT -> BinOperator.LT
                WACCParser.OP_LEQ -> BinOperator.LEQ
                WACCParser.OP_EQ -> BinOperator.EQ
                WACCParser.OP_NEQ -> BinOperator.NEQ
                WACCParser.OP_AND -> BinOperator.AND
                WACCParser.OP_OR -> BinOperator.OR
                else -> throw Exception("Unknown binary operand")
            },
            ctx
        )
    }

    override fun visitExprUnary(ctx: WACCParser.ExprUnaryContext): UnaryOperation {
        return UnaryOperation(
            st,
            safeVisit(Literal(st, WUnknown())) { this.visit(ctx.operand) } as Expr,
            op = when (ctx.unOp.type) {
                WACCParser.OP_NOT -> UnOperator.NOT
                WACCParser.OP_ORD -> UnOperator.ORD
                WACCParser.OP_CHR -> UnOperator.CHR
                WACCParser.OP_LEN -> UnOperator.LEN
                WACCParser.OP_SUBT -> UnOperator.SUB
                else -> throw Exception("Unknown unary operand")
            },
            parserCtx = ctx
        )
    }

    override fun visitExprIdentifier(ctx: WACCParser.ExprIdentifierContext): IdentifierGet {
        return IdentifierGet(st, ctx.IDENTIFIER().text, ctx)
    }

    override fun visitExprLiteral(ctx: WACCParser.ExprLiteralContext): Expr {
        return safeVisit(Literal(st, WUnknown())) { this.visit(ctx.literal()) } as Expr
    }

    override fun visitAssignLhsExpr(ctx: WACCParser.AssignLhsExprContext): IdentifierSet {
        return IdentifierSet(st, ctx.IDENTIFIER().text, ctx)
    }

    override fun visitAssignLhsArrayElem(ctx: WACCParser.AssignLhsArrayElemContext): ArrayElement {
        return this.visit(ctx.arrayElem()) as ArrayElement
    }

    override fun visitAssignLhsPairElem(ctx: WACCParser.AssignLhsPairElemContext): LHS {
        return this.visit(ctx.pairElem()) as LHS
    }

    override fun visitAssignRhsExpr(ctx: WACCParser.AssignRhsExprContext): Expr {
        return safeVisit(Literal(st, WUnknown())) { this.visit(ctx.expr()) } as Expr
    }

    override fun visitAssignRhsArrayLiter(ctx: WACCParser.AssignRhsArrayLiterContext): ArrayLiteral {
        return safeVisit(ArrayLiteral(st, arrayOf(), ctx)) { this.visit(ctx.arrayLiter()) } as ArrayLiteral
    }

    override fun visitAssignRhsNewPair(ctx: WACCParser.AssignRhsNewPairContext): NewPairRHS {
        val leftExpr = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.left) } as Expr
        val rightExpr = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.right) } as Expr
        val type = WPair(leftExpr.type, rightExpr.type)
        return NewPairRHS(st, leftExpr, rightExpr, type)
    }

    override fun visitAssignRhsPairElem(ctx: WACCParser.AssignRhsPairElemContext): RHS {
        val rhs = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.pairElem()) } as RHS
        if (rhs.type is WPairKW) {
            if (rhs is PairElement && rhs.expr is IdentifierGet) {
                val rhsType = st.get(
                    rhs.expr.identifier,
                    builderTemplateFromContext(ctx, st)
                ) as WPair
                val newType = if (rhs.first) rhsType.leftType else rhsType.rightType
                rhs.updateType(newType)
            }
        }
        return rhs
    }

    override fun visitAssignRhsCall(ctx: WACCParser.AssignRhsCallContext): FunctionCall {
        val params: Array<Expr> =
            ctx.argList()?.expr()?.map { arg -> safeVisit(Literal(st, WUnknown())) { this.visit(arg) } as Expr }
                ?.toTypedArray()
                ?: arrayOf()

        return FunctionCall(
            st,
            ctx.IDENTIFIER().text,
            params,
            ctx
        )
    }

    override fun visitArgList(ctx: WACCParser.ArgListContext): AST {
        throw Exception("Don't call me!")
    }

    override fun visitStatInit(ctx: WACCParser.StatInitContext): Declaration {
        val decType = (safeVisit(WACCType(st, WUnknown())) { this.visit(ctx.type()) } as Typed).type
        val rhs = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.assignRhs()) } as RHS
        val identifier = ctx.IDENTIFIER().text
        return Declaration(st, decType, identifier, rhs, ctx)
    }

    override fun visitStatWhileDo(ctx: WACCParser.StatWhileDoContext): WhileStat {
        val conditionExpr = safeVisit(Literal(st, WBool())) { this.visit(ctx.whileCond) } as Expr
        val loopBodyStat = safeVisit(SkipStat(st)) {
            ASTVisitor(st.createChildScope(), semanticErrorCount).visit(ctx.doBlock)
        } as Stat
        return WhileStat(st, conditionExpr, loopBodyStat, ctx)
    }

    override fun visitStatRead(ctx: WACCParser.StatReadContext): ReadStat {
        return ReadStat(
            st,
            this.visit(ctx.assignLhs()) as LHS,
            ctx
        )
    }

    override fun visitStatBeginEnd(ctx: WACCParser.StatBeginEndContext): AST {
        return ASTVisitor(st.createChildScope(), semanticErrorCount).visit(ctx.stat())
    }

    override fun visitStatFree(ctx: WACCParser.StatFreeContext): FreeStat {
        return FreeStat(st, safeVisit(Literal(st, WUnknown())) { this.visit(ctx.expr()) } as Expr, ctx)
    }

    override fun visitStatPrint(ctx: WACCParser.StatPrintContext): PrintStat {
        return PrintStat(st, false, safeVisit(Literal(st, WUnknown())) { this.visit(ctx.expr()) } as Expr)
    }

    override fun visitStatPrintln(ctx: WACCParser.StatPrintlnContext): PrintStat {
        return PrintStat(st, true, safeVisit(Literal(st, WUnknown())) { this.visit(ctx.expr()) } as Expr)
    }

    override fun visitStatExit(ctx: WACCParser.StatExitContext): ExitStat {
        return ExitStat(st, safeVisit(Literal(st, WUnknown())) { this.visit(ctx.expr()) } as Expr, ctx)
    }

    override fun visitStatStore(ctx: WACCParser.StatStoreContext): Assignment {
        val assignLhs = this.visit(ctx.assignLhs()) as LHS
        val assignRhs = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.assignRhs()) } as RHS
        return Assignment(st, assignLhs, assignRhs, ctx)
    }

    override fun visitStatJoin(ctx: WACCParser.StatJoinContext): JoinStat {
        val left: Stat = safeVisit(SkipStat(st)) { this.visit(ctx.left) } as Stat
        val right: Stat = safeVisit(SkipStat(st)) { this.visit(ctx.right) } as Stat
        return JoinStat(st, left, right)
    }

    override fun visitStatSkip(ctx: WACCParser.StatSkipContext): SkipStat {
        return SkipStat(st)
    }

    override fun visitStatReturn(ctx: WACCParser.StatReturnContext): ReturnStat {
        return ReturnStat(st, safeVisit(Literal(st, WUnknown())) { this.visit(ctx.expr()) } as Expr, ctx)
    }

    override fun visitStatIfThenElse(ctx: WACCParser.StatIfThenElseContext): IfThenStat {
        val thenStat = safeVisit(SkipStat(st)) {
            ASTVisitor(st.createChildScope(), semanticErrorCount).visit(ctx.thenBlock)
        } as Stat
        val elseStat = safeVisit(SkipStat(st)) {
            ASTVisitor(st.createChildScope(), semanticErrorCount).visit(ctx.elseBlock)
        } as Stat
        val cond = safeVisit(Literal(st, WBool())) { this.visit(ctx.ifCond) } as Expr
        return IfThenStat(st, cond, thenStat, elseStat, ctx)
    }

    override fun visitParam(ctx: WACCParser.ParamContext): AST {
        throw Exception("Don't call me!")
    }

    override fun visitParamList(ctx: WACCParser.ParamListContext): AST {
        throw Exception("Don't call me!")
    }

    /**
     * Visiting the function parameters to add its type signature later
     * @return WACCFunction which has all the fields but the Abstract
     * Syntax tree corresponding to the body of that function
     */
    private fun visitFuncParams(ctx: WACCParser.FuncContext): WACCFunction {
        val params: MutableMap<String, WAny> = mutableMapOf()
        val funScope = st.createChildScope()
        if (ctx.paramList() != null) {
            for (p in ctx.paramList().param()) {
                val id = p.IDENTIFIER().text
                val ty = (safeVisit(WACCType(st, WUnknown())) { this.visit(p.type()) } as WACCType).type
                params[id] = ty
                funScope.declare(
                    id,
                    ty,
                    builderTemplateFromContext(ctx, st)
                )
            }
        }
        return WACCFunction(
            funScope,
            ctx.IDENTIFIER().text,
            params,
            SkipStat(st),
            (safeVisit(WACCType(st, WUnknown())) { this.visit(ctx.type()) } as WACCType).type,
            ctx
        )
    }

    /**
     * Visit the function body after the function type and params were already visited
     */
    private fun visitFuncBody(function: WACCFunction, ctx: WACCParser.FuncContext): WACCFunction {
        return safeVisit(function) {
            WACCFunction(
                function.st,
                function.identifier,
                function.params,
                safeVisit(SkipStat(st)) {
                    ASTVisitor(function.st, semanticErrorCount).visit(ctx.stat())
                } as Stat,
                function.type,
                ctx
            )
        } as WACCFunction
    }

    /**
     * The following function is not used, because the function is visited by parts.
     * First, its type and parameters are visited and then its body using two functions.
     */
    override fun visitFunc(ctx: WACCParser.FuncContext): WACCFunction {
        throw Exception("Don't call me!")
    }

}