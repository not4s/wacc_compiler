package semantic

import antlr.WACCParser
import antlr.WACCParserBaseVisitor
import ast.*
import ast.statement.*
import symbolTable.ParentRefSymbolTable
import symbolTable.SymbolTable
import syntax.SyntaxChecker
import utils.*
import waccType.*
import java.util.concurrent.atomic.AtomicInteger

class ASTProducer(
    private val st: SymbolTable,
) : WACCParserBaseVisitor<AST>() {

    private var semanticErrorCount: AtomicInteger = AtomicInteger(0)

    /**
     * Used for transferring the Boolean value of the fact of semantic error occurrence
     */
    private constructor(st: SymbolTable, errorAlreadyOccurred: AtomicInteger) : this(st) {
        semanticErrorCount = errorAlreadyOccurred
    }

    /**
     * Wrapper around try-catch block to write less repetitive code
     * @param default is the AST which is returned if an error occurred
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

    override fun visitProgram(ctx: WACCParser.ProgramContext): ProgramAST {
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
                    errorMessageBuilder = builderTemplateFromContext(ctx, st)
                )
            } catch (e: SemanticException) {
                semanticErrorCount.incrementAndGet()
            }
            waccFunctions.add(Pair(f, bodyLessFunction))
        }
        val funcASTs = mutableListOf<WACCFunction>()
        for ((funCtx, waccFun) in waccFunctions) {
            val funcAST = safeVisit(waccFun) { visitFuncBody(waccFun, funCtx) } as WACCFunction
            st.reassign(
                funcAST.identifier,
                funcAST,
                builderTemplateFromContext(funCtx, st)
            )
            funcASTs.add(funcAST)
        }
        // Create a child scope, functions are now stored in parent table.
        // This scope is still 'global'
        val childScope = st.createChildScope()
        childScope.isGlobal = true
        val programBody =
            safeVisit(SkipStat(st)) { ASTProducer(childScope, semanticErrorCount).visit(ctx.stat()) } as Stat
        val totalSemanticErrors = semanticErrorCount.get()
        if (totalSemanticErrors > 0) {
            throw SemanticException("Semantic errors detected: $totalSemanticErrors, compilation aborted.")
        }
        return ProgramAST(st, funcASTs, programBody)
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

    /**
     * Visiting types like <something[]>[]
     */
    override fun visitArrayTypeArrayType(ctx: WACCParser.ArrayTypeArrayTypeContext): WACCType {
        val elemType: WACCType = safeVisit(WACCType(st, WUnknown())) { this.visit(ctx.arrayType()) } as WACCType
        return WACCType(st, WArray(elemType.type))
    }

    /**
     * Visiting types like int[], str[], char[], bool[]
     */
    override fun visitArrayTypeBaseType(ctx: WACCParser.ArrayTypeBaseTypeContext): WACCType {
        val elemType: WACCType = safeVisit(WACCType(st, WUnknown())) { this.visit(ctx.baseType()) } as WACCType
        return WACCType(st, WArray(elemType.type))
    }

    /**
     * Visiting types like pair[]
     */
    override fun visitArrayTypePairType(ctx: WACCParser.ArrayTypePairTypeContext): AST {
        val elemType: WACCType = safeVisit(WACCType(st, WUnknown())) { this.visit(ctx.pairType()) } as WACCType
        return WACCType(st, WArray(elemType.type))
    }

    override fun visitArrayElem(ctx: WACCParser.ArrayElemContext): ArrayElement {
        val indices: Array<Expr> = ctx.expr().map {
            safeVisit(Literal(st, WUnknown())) { this.visit(it) } as Expr
        }.toTypedArray()
        SemanticChecker.checkThatAllIndicesAreWInts(indices, builderTemplateFromContext(ctx, st))
        return ArrayElement(st, ctx.IDENTIFIER().text, indices, ctx)
    }

    override fun visitArrayLiterAssignRhs(ctx: WACCParser.ArrayLiterAssignRhsContext): ArrayLiteral {
        val elements: Array<Expr> = ctx.expr().map {
            (safeVisit(Literal(st, WUnknown())) { this.visit(it) } as Expr)
        }.toTypedArray()
        SemanticChecker.checkThatAllArrayElementsHaveTheSameType(elements, builderTemplateFromContext(ctx, st))
        return ArrayLiteral(st, elements)
    }

    /**
     * Visits the first or second element access of the pair.
     */
    private fun visitPair(
        ctx: WACCParser.PairElemContext,
        ctxExpr: WACCParser.ExprContext,
        isFirst: Boolean
    ): PairElement {
        val expr = safeVisit(Literal(st, WUnknown())) { this.visit(ctxExpr) } as Expr
        SemanticChecker.checkTheExprIsPairAndNoNullDereference(expr, isFirst, builderTemplateFromContext(ctx, st))
        return PairElement(st, isFirst, expr, ctx)
    }

    override fun visitPairElemFst(ctx: WACCParser.PairElemFstContext): PairElement {
        return visitPair(ctx, ctx.expr(), isFirst = true)
    }

    override fun visitPairElemSnd(ctx: WACCParser.PairElemSndContext): PairElement {
        return visitPair(ctx, ctx.expr(), isFirst = false)
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

    override fun visitLiteralInteger(ctx: WACCParser.LiteralIntegerContext): Literal {
        SyntaxChecker.assertIntFitsTheRange(ctx, st)
        val value: Int = Integer.parseInt(ctx.text)
        return Literal(st, WInt(value))
    }

    override fun visitLiteralBoolean(ctx: WACCParser.LiteralBooleanContext): Literal {
        val value: Boolean = ctx.value.type == WACCParser.KW_TRUE
        return Literal(st, WBool(value))
    }

    override fun visitLiteralChar(ctx: WACCParser.LiteralCharContext): Literal {
        val value: Char = ctx.CHAR().text.dropLast(1).last()
        return Literal(st, WChar(value))
    }

    override fun visitLiteralString(ctx: WACCParser.LiteralStringContext): Literal {
        val value: String = ctx.STRING().text.substring(1, ctx.STRING().text.length - 1)
        return Literal(st, WStr(value))
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
        val errorMessageBuilder = builderTemplateFromContext(ctx, st)
        val left = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.left) } as Expr
        val right = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.right) } as Expr
        val op = BinOperator.fromWACCParserContextBinOp(ctx.binOp)
        SemanticChecker.checkThatOperandTypesMatch(
            firstType = left.type,
            secondType = right.type,
            errorMessageBuilder= errorMessageBuilder,
            extraMessage = "Binary operation cannot be executed correctly",
            failMessage = "Attempted to call binary operation $op on unequal types: ${left.type}, ${right.type}"
        )
        SemanticChecker.checkThatOperationTypeIsValid(
            operandType = left.type,
            errorMessageBuilder = errorMessageBuilder,
            operation = op
        )
        return BinaryOperation(st, left, right, op)
    }

    override fun visitExprUnary(ctx: WACCParser.ExprUnaryContext): UnaryOperation {
        val operandExpr = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.operand) } as Expr
        val unaryOperation = UnOperator.fromWACCParserContextUnOp(ctx.unOp)
        val errorMessageBuilder = builderTemplateFromContext(ctx, st)
        SemanticChecker.checkThatOperationTypeIsValid(operandExpr.type, errorMessageBuilder, unaryOperation)
        return UnaryOperation(st, operandExpr, unaryOperation)
    }

    /**
     * NOTE:
     * Here the SemanticChecker statement was removed as redundant. Maybe worth looking at this function
     * if some unknown bug will show up.
     */
    override fun visitExprIdentifier(ctx: WACCParser.ExprIdentifierContext): IdentifierGet {
        val symbol = ctx.IDENTIFIER().text
        st.get(symbol, builderTemplateFromContext(ctx, st))
        return IdentifierGet(st, symbol, ctx)
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
        return safeVisit(ArrayLiteral(st, arrayOf())) { this.visit(ctx.arrayLiter()) } as ArrayLiteral
    }

    override fun visitAssignRhsNewPair(ctx: WACCParser.AssignRhsNewPairContext): NewPairRHS {
        val leftExpr = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.left) } as Expr
        val rightExpr = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.right) } as Expr
        val type = WPair(leftExpr.type, rightExpr.type)
        return NewPairRHS(st, leftExpr, rightExpr, type)
    }

    /**
     * The following function visits the node but also updates the type of the pair from IncompleteWPair to WPair
     */
    override fun visitAssignRhsPairElem(ctx: WACCParser.AssignRhsPairElemContext): RHS {
        val rhs = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.pairElem()) } as RHS
        if (rhs.type !is WPairKW || !(rhs is PairElement && rhs.expr is IdentifierGet)) {
            return rhs
        }
        val rhsType = st.get(
            rhs.expr.identifier,
            builderTemplateFromContext(ctx, st)
        ) as WPair
        val newType = if (rhs.first) rhsType.leftType else rhsType.rightType
        rhs.updateType(newType)
        return rhs
    }

    override fun visitAssignRhsCall(ctx: WACCParser.AssignRhsCallContext): FunctionCall {
        val errorMessageBuilder = builderTemplateFromContext(ctx, st)
        val params: Array<Expr> = ctx.argList()?.expr()
            ?.map { arg ->
                safeVisit(Literal(st, WUnknown())) { this.visit(arg) } as Expr
            }?.toTypedArray()
            ?: arrayOf()
        val identifier: String = ctx.IDENTIFIER().text
        val func = st.get(identifier, errorMessageBuilder) as WACCFunction
        SemanticChecker.checkFunctionCall(func, params, errorMessageBuilder, identifier)
        return FunctionCall(st, identifier, params, ctx)
    }

    override fun visitArgList(ctx: WACCParser.ArgListContext): AST {
        throw Exception("Don't call me!")
    }

    override fun visitStatInit(ctx: WACCParser.StatInitContext): Declaration {
        val decType = (safeVisit(WACCType(st, WUnknown())) { this.visit(ctx.type()) } as Typed).type
        val rhs = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.assignRhs()) } as RHS
        val identifier = ctx.IDENTIFIER().text
        SemanticChecker.checkThatOperandTypesMatch(
            firstType = decType,
            secondType = rhs.type,
            errorMessageBuilder = builderTemplateFromContext(ctx, st),
            extraMessage = "The type of variable $identifier and the evaluated expression do not match",
            failMessage = "Attempted to declare variable $identifier of type $decType to ${rhs.type}"
        )
        return Declaration(st, decType, identifier, rhs, ctx)
    }

    override fun visitStatWhileDo(ctx: WACCParser.StatWhileDoContext): WhileStat {
        val conditionExpr = safeVisit(Literal(st, WBool())) { this.visit(ctx.whileCond) } as Expr
        val loopBodyStat = safeVisit(SkipStat(st)) {
            ASTProducer(st.createChildScope(), semanticErrorCount).visit(ctx.doBlock)
        } as Stat
        SemanticChecker.checkWhileCondIsWBool(
            type = conditionExpr.type,
            errorMessageBuilder = builderTemplateFromContext(ctx, st),
            failMessage = "While loop has non-bool condition, actual: ${conditionExpr.type}"
        )
        return WhileStat(st, conditionExpr, loopBodyStat)
    }

    override fun visitStatRead(ctx: WACCParser.StatReadContext): ReadStat {
        val lhs = this.visit(ctx.assignLhs()) as LHS
        SemanticChecker.checkReadType(
            type = lhs.type,
            errorMessageBuilder = builderTemplateFromContext(ctx, st),
            failMessage = "Cannot read into non-char or non-int variable, actual: ${lhs.type}"
        )
        return ReadStat(st, lhs)
    }

    override fun visitStatBeginEnd(ctx: WACCParser.StatBeginEndContext): AST {
        return ASTProducer(st.createChildScope(), semanticErrorCount).visit(ctx.stat())
    }

    override fun visitStatFree(ctx: WACCParser.StatFreeContext): FreeStat {
        val expression = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.expr()) } as Expr
        SemanticChecker.checkExprTypeIsWPair(
            type = expression.type,
            errorMessageBuilder = builderTemplateFromContext(ctx, st),
            failMessage = "This isn't C, you can't free a $expression"
        )
        return FreeStat(st, expression)
    }

    override fun visitStatPrint(ctx: WACCParser.StatPrintContext): PrintStat {
        return PrintStat(st, false, safeVisit(Literal(st, WUnknown())) { this.visit(ctx.expr()) } as Expr)
    }

    override fun visitStatPrintln(ctx: WACCParser.StatPrintlnContext): PrintStat {
        return PrintStat(st, true, safeVisit(Literal(st, WUnknown())) { this.visit(ctx.expr()) } as Expr)
    }

    override fun visitStatExit(ctx: WACCParser.StatExitContext): ExitStat {
        val expression = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.expr()) } as Expr
        SemanticChecker.checkExprTypeIsWInt(
            type = expression.type,
            errorMessageBuilder = builderTemplateFromContext(ctx, st),
            failMessage = "Cannot exit with non-int expression. Actual: ${expression.type}"
        )
        return ExitStat(st, expression)
    }

    override fun visitStatStore(ctx: WACCParser.StatStoreContext): Assignment {
        val lhs = this.visit(ctx.assignLhs()) as LHS
        val rhs = safeVisit(Literal(st, WUnknown())) { this.visit(ctx.assignRhs()) } as RHS
        val errorMessageBuilder = builderTemplateFromContext(ctx, st)
        SemanticChecker.checkAssignment(lhs, rhs, st, errorMessageBuilder)
        return Assignment(st, lhs, rhs)
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
        SemanticChecker.checkReturnFromGlobalScope(st, builderTemplateFromContext(ctx, st))
        return ReturnStat(st, safeVisit(Literal(st, WUnknown())) { this.visit(ctx.expr()) } as Expr)
    }

    override fun visitStatIfThenElse(ctx: WACCParser.StatIfThenElseContext): IfThenStat {
        val thenStat = safeVisit(SkipStat(st)) {
            ASTProducer(st.createChildScope(), semanticErrorCount).visit(ctx.thenBlock)
        } as Stat
        val elseStat = safeVisit(SkipStat(st)) {
            ASTProducer(st.createChildScope(), semanticErrorCount).visit(ctx.elseBlock)
        } as Stat
        val condition = safeVisit(Literal(st, WBool())) { this.visit(ctx.ifCond) } as Expr
        SemanticChecker.checkIfCondIsWBool(
            type = condition.type,
            errorMessageBuilder = builderTemplateFromContext(ctx, st),
            failMessage = "If statement has non-bool condition, actual: ${condition.type}"
        )
        return IfThenStat(st, condition, thenStat, elseStat)
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
        val funScope = st.createChildScope() as ParentRefSymbolTable
        funScope.forceOffset = -4 // Accounts for extra space between LR and stack frame.
        if (ctx.paramList() != null) {
            for (p in ctx.paramList().param()) {
                val id = p.IDENTIFIER().text
                funScope.redeclaredVars.add(id)
                val ty = (safeVisit(WACCType(st, WUnknown())) { this.visit(p.type()) } as WACCType).type
                params[id] = ty
                funScope.declare(id, ty, builderTemplateFromContext(ctx, st))
            }
        }
        return WACCFunction(
            funScope.createChildScope(),
            ctx.IDENTIFIER().text,
            params,
            SkipStat(st),
            (safeVisit(WACCType(st, WUnknown())) { this.visit(ctx.type()) } as WACCType).type
        )
    }

    /**
     * Visit the function body after the function type and params were already visited
     */
    private fun visitFuncBody(function: WACCFunction, ctx: WACCParser.FuncContext): WACCFunction {
        val functionBody =
            safeVisit(SkipStat(st)) {
                ASTProducer(function.st, semanticErrorCount).visit(ctx.stat())
            } as Stat
        SemanticChecker.checkReturnType(functionBody, function.type, builderTemplateFromContext(ctx, st))
        SyntaxChecker.checkFunctionHavingReturn(functionBody, function.identifier)

        return WACCFunction(function.st, function.identifier, function.params, functionBody, function.type)
    }

    /**
     * The following function is not used, because the function is visited by parts.
     * First, its type and parameters are visited and then its body using two functions.
     */
    override fun visitFunc(ctx: WACCParser.FuncContext): WACCFunction {
        throw Exception("Don't call me!")
    }
}