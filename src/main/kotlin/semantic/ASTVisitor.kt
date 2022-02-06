package semantic

import antlr.WACCParser
import antlr.WACCParserBaseVisitor
import ast.*
import symbolTable.SymbolTable
import waccType.*

class ASTVisitor(val st: SymbolTable) : WACCParserBaseVisitor<AST>() {

    override fun visitProgram(ctx: WACCParser.ProgramContext): Stat {
        /* TODO: Visit funcs */
        return this.visit(ctx.stat()) as Stat
    }

    override fun visitTypeBaseType(ctx: WACCParser.TypeBaseTypeContext): WACCType {
        return this.visit(ctx.baseType()) as WACCType
    }

    override fun visitTypeArrayType(ctx: WACCParser.TypeArrayTypeContext): WACCType {
        return this.visit(ctx.arrayType()) as WACCType
    }

    override fun visitTypePairType(ctx: WACCParser.TypePairTypeContext): AST {
        TODO()
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

    override fun visitArrayTypePairType(ctx: WACCParser.ArrayTypePairTypeContext): AST {
        TODO()
    }

    override fun visitArrayElem(ctx: WACCParser.ArrayElemContext): ArrayElement {
        val indices: Array<Expr> = ctx.expr().map { e -> this.visit(e) as Expr }.toTypedArray()
        return ArrayElement(st, ctx.IDENTIFIER().text, indices, WUnknown())
    }

    override fun visitArrayLiterAssignRhs(ctx: WACCParser.ArrayLiterAssignRhsContext): ArrayLiteral {
        val elems: Array<WAny> =
            ctx.expr().map { e -> (this.visit(e) as Expr).type }.toTypedArray()
        return ArrayLiteral(st, elems, WArray(WUnknown()))
    }

    override fun visitPairLiter(ctx: WACCParser.PairLiterContext): PairLiteral {
        TODO()
    }

    override fun visitPairElemFst(ctx: WACCParser.PairElemFstContext): AST {
        TODO()
    }

    override fun visitPairElemSnd(ctx: WACCParser.PairElemSndContext): AST {
        TODO()
    }

    override fun visitPairType(ctx: WACCParser.PairTypeContext): AST {
        TODO()
    }

    override fun visitPairElemTypeBaseType(ctx: WACCParser.PairElemTypeBaseTypeContext): AST {
        TODO()
    }

    override fun visitPairElemTypeArrayType(ctx: WACCParser.PairElemTypeArrayTypeContext): AST {
        TODO()
    }

    override fun visitPairElemTypeKwPair(ctx: WACCParser.PairElemTypeKwPairContext): AST {
        TODO()
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
        TODO()
    }

    override fun visitExprBracket(ctx: WACCParser.ExprBracketContext): Expr {
        return this.visit(ctx.expr()) as Expr
    }

    override fun visitExprArrayElem(ctx: WACCParser.ExprArrayElemContext): ArrayElement {
        return this.visit(ctx.arrayElem()) as ArrayElement
    }

    override fun visitExprBinary(ctx: WACCParser.ExprBinaryContext): BinaryOperation {
        return BinaryOperation(st,
            this.visit(ctx.left) as Expr,
            this.visit(ctx.right) as Expr,
            when (ctx.binOp.text) {
                // Oh no
                "*" -> BinOperator.MUL
                "/" -> BinOperator.DIV
                "%" -> BinOperator.MOD
                "+" -> BinOperator.ADD
                "-" -> BinOperator.SUB
                ">" -> BinOperator.GT
                ">=" -> BinOperator.GEQ
                "<" -> BinOperator.LT
                "<=" -> BinOperator.LEQ
                "==" -> BinOperator.EQ
                "!=" -> BinOperator.NEQ
                "&&" -> BinOperator.AND
                "||" -> BinOperator.OR
                else -> throw Exception("What.")
            })
    }

    override fun visitExprUnary(ctx: WACCParser.ExprUnaryContext): UnaryOperation {
        return UnaryOperation(st, this.visit(ctx.operand) as Expr, when (ctx.unOp.text) {
            // Oh no again
            "!" -> UnOperator.NOT
            "ord" -> UnOperator.ORD
            "chr" -> UnOperator.CHR
            "len" -> UnOperator.LEN
            "-" -> UnOperator.SUB
            else -> throw Exception("What.")
        })
    }


    override fun visitExprIdentifier(ctx: WACCParser.ExprIdentifierContext): Identifer {
        return Identifer(st, ctx.IDENTIFIER().text, WUnknown())
    }

    override fun visitExprLiteral(ctx: WACCParser.ExprLiteralContext): Literal {
        return this.visit(ctx.literal()) as Literal
    }

    override fun visitAssignLhsExpr(ctx: WACCParser.AssignLhsExprContext): Identifer {
        return Identifer(st, ctx.IDENTIFIER().text, WUnknown())
    }

    override fun visitAssignLhsArrayElem(ctx: WACCParser.AssignLhsArrayElemContext): ArrayElement {
        return this.visit(ctx.arrayElem()) as ArrayElement
    }

    override fun visitAssignLhsPairElem(ctx: WACCParser.AssignLhsPairElemContext): Expr {
        TODO()
    }

    override fun visitAssignRhsExpr(ctx: WACCParser.AssignRhsExprContext): Expr {
        return this.visit(ctx.expr()) as Expr
    }

    override fun visitAssignRhsArrayLiter(ctx: WACCParser.AssignRhsArrayLiterContext): ArrayLiteral {
        return this.visit(ctx.arrayLiter()) as ArrayLiteral
    }

    override fun visitAssignRhsNewPair(ctx: WACCParser.AssignRhsNewPairContext): AST {
        TODO()
    }

    override fun visitAssignRhsPairElem(ctx: WACCParser.AssignRhsPairElemContext): AST {
        TODO()
    }

    override fun visitAssignRhsCall(ctx: WACCParser.AssignRhsCallContext): AST {
        TODO()
    }

    override fun visitArgList(ctx: WACCParser.ArgListContext): AST {
        TODO()
    }

    override fun visitStatInit(ctx: WACCParser.StatInitContext): Declaration {
        return Declaration(st,
            (this.visit(ctx.type()) as Typed).type,
            ctx.IDENTIFIER().text,
            this.visit(ctx.assignRhs()) as RHS)
    }

    override fun visitStatWhileDo(ctx: WACCParser.StatWhileDoContext): WhileStat {
        return WhileStat(st,
            this.visit(ctx.whileCond) as Expr,
            ASTVisitor(st.createChildScope()).visit(ctx.doBlock) as Stat)
    }

    override fun visitStatRead(ctx: WACCParser.StatReadContext): AST {
        TODO()
    }

    override fun visitStatBeginEnd(ctx: WACCParser.StatBeginEndContext): AST {
        return ASTVisitor(st.createChildScope()).visit(ctx.stat())
    }

    override fun visitStatFree(ctx: WACCParser.StatFreeContext): AST {
        TODO()
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
        return Assignment(st,
            this.visit(ctx.assignLhs()) as LHS,
            this.visit(ctx.assignRhs()) as RHS)
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
        return IfThenStat(st,
            this.visit(ctx.ifCond) as Expr,
            // Create child scopes for the if-then-else blocks
            ASTVisitor(st.createChildScope()).visit(ctx.thenBlock) as Stat,
            ASTVisitor(st.createChildScope()).visit(ctx.elseBlock) as Stat)
    }

    override fun visitParam(ctx: WACCParser.ParamContext): AST {
        TODO()
    }

    override fun visitParamList(ctx: WACCParser.ParamListContext): AST {
        TODO()
    }

    override fun visitFunc(ctx: WACCParser.FuncContext): AST {
        TODO()
    }
}