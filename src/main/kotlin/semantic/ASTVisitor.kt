package semantic

import antlr.WACCParser
import antlr.WACCParserBaseVisitor
import ast.*
import symbolTable.ParentRefSymbolTable
import symbolTable.SymbolTable
import utils.ExitCode
import utils.SemanticException
import waccType.*
import kotlin.system.exitProcess

class ASTVisitor(val st: SymbolTable) : WACCParserBaseVisitor<AST>() {

    override fun visitProgram(ctx: WACCParser.ProgramContext): Stat {
        st as ParentRefSymbolTable
        // This adds functions to st
        for (f in ctx.func()) {
            val id = f.IDENTIFIER().text
            if (id in st.dict) {
                throw SemanticException("Cannot redefine function $id")
            }
            st.dict[id] = WACCFunction(st, id, mapOf(), SkipStat(st), WUnknown())
        }
        ctx.func()
            .map { this.visit(it) as WACCFunction }
            .forEach { st.dict[it.ident] = it }

        // Explicitly call checks after defining all functions
        st.dict.forEach { (_, f) -> (f as WACCFunction).check() }
        // Create a subscope, functions are now stored in parent table.
        // This scope is still 'global'
        val subscope = st.createChildScope()
        subscope.isGlobal = true
        return ASTVisitor(subscope).visit(ctx.stat()) as Stat
    }

    override fun visitTypeBaseType(ctx: WACCParser.TypeBaseTypeContext): WACCType {
        return this.visit(ctx.baseType()) as WACCType
    }

    override fun visitTypeArrayType(ctx: WACCParser.TypeArrayTypeContext): WACCType {
        return this.visit(ctx.arrayType()) as WACCType
    }

    override fun visitTypePairType(ctx: WACCParser.TypePairTypeContext): WACCType {
        return this.visit(ctx.pairType()) as WACCType
    }

    // <something[]>[]
    override fun visitArrayTypeArrayType(ctx: WACCParser.ArrayTypeArrayTypeContext): WACCType {
        val elemType: WACCType = this.visit(ctx.arrayType()) as WACCType
        return WACCType(st, WArray(elemType.type))
    }

    // int[], str[], char[], bool[]
    override fun visitArrayTypeBaseType(ctx: WACCParser.ArrayTypeBaseTypeContext): WACCType {
        val elemType: WACCType = this.visit(ctx.baseType()) as WACCType
        return WACCType(st, WArray(elemType.type))
    }

    // pair[]
    override fun visitArrayTypePairType(ctx: WACCParser.ArrayTypePairTypeContext): AST {
        val elemType: WACCType = this.visit(ctx.pairType()) as WACCType
        return WACCType(st, WArray(elemType.type))
    }

    override fun visitArrayElem(ctx: WACCParser.ArrayElemContext): ArrayElement {
        val indices: Array<Expr> = ctx.expr().map { e -> this.visit(e) as Expr }.toTypedArray()
        return ArrayElement(st, ctx.IDENTIFIER().text, indices)
    }

    override fun visitArrayLiterAssignRhs(ctx: WACCParser.ArrayLiterAssignRhsContext): ArrayLiteral {
        val elems: Array<WAny> = ctx.expr().map { e -> (this.visit(e) as Expr).type }.toTypedArray()
        return ArrayLiteral(st, elems, WArray(WUnknown()))
    }

    override fun visitPairLiter(ctx: WACCParser.PairLiterContext): PairLiteral {
        return PairLiteral(st, WPair(WUnknown(), WUnknown()))
    }

    override fun visitPairElemFst(ctx: WACCParser.PairElemFstContext): AST {
        val expr = this.visit(ctx.expr()) as Expr
        return PairElement(st, true, expr)
    }

    override fun visitPairElemSnd(ctx: WACCParser.PairElemSndContext): AST {
        val expr = this.visit(ctx.expr()) as Expr
        return PairElement(st, false, expr)
    }

    override fun visitPairType(ctx: WACCParser.PairTypeContext): WACCType {
        val left = this.visit(ctx.left) as WACCType
        val right = this.visit(ctx.right) as WACCType
        return WACCType(st, WPair(left.type, right.type))
    }

    override fun visitPairElemTypeBaseType(ctx: WACCParser.PairElemTypeBaseTypeContext): WACCType {
        return this.visit(ctx.baseType()) as WACCType
    }

    override fun visitPairElemTypeArrayType(ctx: WACCParser.PairElemTypeArrayTypeContext): WACCType {
        return this.visit(ctx.arrayType()) as WACCType
    }

    override fun visitPairElemTypeKwPair(ctx: WACCParser.PairElemTypeKwPairContext): WACCType {
        return WACCType(st, WPair(WUnknown(), WUnknown()))
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

    override fun visitLiteralInteger(ctx: WACCParser.LiteralIntegerContext): Literal {
        // make sure int is within bounds immediately
        try {
            Integer.parseInt(ctx.text)
        } catch (e: java.lang.NumberFormatException) {
            println("Attempted to parse a very big int!")
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
        return PairLiteral(st, WPair(WUnknown(), WUnknown()))
    }

    override fun visitExprBracket(ctx: WACCParser.ExprBracketContext): Expr {
        return this.visit(ctx.expr()) as Expr
    }

    override fun visitExprArrayElem(ctx: WACCParser.ExprArrayElemContext): ArrayElement {
        return this.visit(ctx.arrayElem()) as ArrayElement
    }

    override fun visitExprBinary(ctx: WACCParser.ExprBinaryContext): BinaryOperation {
        return BinaryOperation(
            st,
            this.visit(ctx.left) as Expr,
            this.visit(ctx.right) as Expr,
            when (ctx.binOp.type) {
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
                WACCParser.OP_OR-> BinOperator.OR
                else -> throw Exception("Unknown binary operand")
            }
        )
    }

    override fun visitExprUnary(ctx: WACCParser.ExprUnaryContext): UnaryOperation {
        return UnaryOperation(
            st, this.visit(ctx.operand) as Expr, when (ctx.unOp.type) {
                WACCParser.OP_NOT -> UnOperator.NOT
                WACCParser.OP_ORD -> UnOperator.ORD
                WACCParser.OP_CHR-> UnOperator.CHR
                WACCParser.OP_LEN -> UnOperator.LEN
                WACCParser.OP_SUBT -> UnOperator.SUB
                else -> throw Exception("Unknown unary operand")
            }
        )
    }

    override fun visitExprIdentifier(ctx: WACCParser.ExprIdentifierContext): IdentifierGet {
        return IdentifierGet(st, ctx.IDENTIFIER().text)
    }

    override fun visitExprLiteral(ctx: WACCParser.ExprLiteralContext): Expr {
        return this.visit(ctx.literal()) as Expr
    }

    override fun visitAssignLhsExpr(ctx: WACCParser.AssignLhsExprContext): IdentifierSet {
        return IdentifierSet(st, ctx.IDENTIFIER().text)
    }

    override fun visitAssignLhsArrayElem(ctx: WACCParser.AssignLhsArrayElemContext): ArrayElement {
        return this.visit(ctx.arrayElem()) as ArrayElement
    }

    override fun visitAssignLhsPairElem(ctx: WACCParser.AssignLhsPairElemContext): LHS {
        return this.visit(ctx.pairElem()) as LHS
    }

    override fun visitAssignRhsExpr(ctx: WACCParser.AssignRhsExprContext): Expr {
        return this.visit(ctx.expr()) as Expr
    }

    override fun visitAssignRhsArrayLiter(ctx: WACCParser.AssignRhsArrayLiterContext): ArrayLiteral {
        return this.visit(ctx.arrayLiter()) as ArrayLiteral
    }

    override fun visitAssignRhsNewPair(ctx: WACCParser.AssignRhsNewPairContext): NewPairRHS {
        return NewPairRHS(
            st,
            this.visit(ctx.left) as Expr,
            this.visit(ctx.right) as Expr,
            WPair(WUnknown(), WUnknown())
        )
    }

    override fun visitAssignRhsPairElem(ctx: WACCParser.AssignRhsPairElemContext): RHS {
        return this.visit(ctx.pairElem()) as RHS
    }

    override fun visitAssignRhsCall(ctx: WACCParser.AssignRhsCallContext): FunctionCall {
        val params: Array<Expr>
            = ctx.argList()?.expr()?.map { arg -> this.visit(arg) as Expr }?.toTypedArray()
            ?: arrayOf()

        return FunctionCall(
            st,
            ctx.IDENTIFIER().text,
            params
        )
    }

    override fun visitArgList(ctx: WACCParser.ArgListContext): AST {
        throw Exception("Don't call me!")
    }

    override fun visitStatInit(ctx: WACCParser.StatInitContext): Declaration {
        return Declaration(
            st,
            (this.visit(ctx.type()) as Typed).type,
            ctx.IDENTIFIER().text,
            this.visit(ctx.assignRhs()) as RHS
        )
    }

    override fun visitStatWhileDo(ctx: WACCParser.StatWhileDoContext): WhileStat {
        return WhileStat(
            st,
            this.visit(ctx.whileCond) as Expr,
            ASTVisitor(st.createChildScope()).visit(ctx.doBlock) as Stat
        )
    }

    override fun visitStatRead(ctx: WACCParser.StatReadContext): ReadStat {
        return ReadStat(st, this.visit(ctx.assignLhs()) as LHS)
    }

    override fun visitStatBeginEnd(ctx: WACCParser.StatBeginEndContext): AST {
        return ASTVisitor(st.createChildScope()).visit(ctx.stat())
    }

    override fun visitStatFree(ctx: WACCParser.StatFreeContext): FreeStat {
        return FreeStat(st, this.visit(ctx.expr()) as Expr)
    }

    override fun visitStatPrint(ctx: WACCParser.StatPrintContext): PrintStat {
        return PrintStat(st, false, this.visit(ctx.expr()) as Expr)
    }

    override fun visitStatPrintln(ctx: WACCParser.StatPrintlnContext): PrintStat {
        return PrintStat(st, true, this.visit(ctx.expr()) as Expr)
    }

    override fun visitStatExit(ctx: WACCParser.StatExitContext): ExitStat {
        return ExitStat(st, this.visit(ctx.expr()) as Expr)
    }

    override fun visitStatStore(ctx: WACCParser.StatStoreContext): Assignment {
        return Assignment(
            st,
            this.visit(ctx.assignLhs()) as LHS,
            this.visit(ctx.assignRhs()) as RHS
        )
    }

    override fun visitStatJoin(ctx: WACCParser.StatJoinContext): JoinStat {
        return JoinStat(st, this.visit(ctx.left) as Stat, this.visit(ctx.right) as Stat)
    }

    override fun visitStatSkip(ctx: WACCParser.StatSkipContext): SkipStat {
        return SkipStat(st)
    }

    override fun visitStatReturn(ctx: WACCParser.StatReturnContext): ReturnStat {
        return ReturnStat(st, this.visit(ctx.expr()) as Expr)
    }

    override fun visitStatIfThenElse(ctx: WACCParser.StatIfThenElseContext): IfThenStat {
        return IfThenStat(
            st,
            this.visit(ctx.ifCond) as Expr,
            // Create child scopes for the if-then-else blocks
            ASTVisitor(st.createChildScope()).visit(ctx.thenBlock) as Stat,
            ASTVisitor(st.createChildScope()).visit(ctx.elseBlock) as Stat
        )
    }

    override fun visitParam(ctx: WACCParser.ParamContext): AST {
        throw Exception("Don't call me!")
    }

    override fun visitParamList(ctx: WACCParser.ParamListContext): AST {
        throw Exception("Don't call me!")
    }

    override fun visitFunc(ctx: WACCParser.FuncContext): WACCFunction {
        val params: MutableMap<String, WAny> = mutableMapOf()
        val funScope = st.createChildScope()
        if (ctx.paramList() != null) {
            for (p in ctx.paramList().param()) {
                val id = p.IDENTIFIER().text
                val ty = (this.visit(p.type()) as WACCType).type
                params[id] = ty
                funScope.declare(id, ty)
            }
        }
        return WACCFunction(
            funScope,
            ctx.IDENTIFIER().text,
            params.toMap(),
            ASTVisitor(funScope).visit(ctx.stat()) as Stat,
            (this.visit(ctx.type()) as WACCType).type
        )
    }
}