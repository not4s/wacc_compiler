package ast

import antlr.WACCParser.*
import antlr.WACCParserBaseVisitor
import utils.raiseSemanticErrorAndExit

class AstProducerVisitor : WACCParserBaseVisitor<AbstractSyntaxTree>() {

    private fun visitStat(statContext: StatContext?): StatAST {
        statContext ?: raiseSemanticErrorAndExit()
        statContext.let {
            return when (statContext) {
                is StatExitContext -> visitStatExit(it as StatExitContext)
                is StatFreeContext -> visitStatFree(it as StatFreeContext)
                is StatSkipContext -> visitStatSkip(it as StatSkipContext)
                is StatReturnContext -> visitStatReturn(it as StatReturnContext)
                is StatPrintContext -> visitStatPrint(it as StatPrintContext)
                is StatPrintlnContext -> visitStatPrintln(it as StatPrintlnContext)
                is StatIfThenElseContext -> visitStatIfThenElse(it as StatIfThenElseContext)
                is StatWhileDoContext -> visitStatWhileDo(it as StatWhileDoContext)
                is StatJoinContext -> visitStatJoin(it as StatJoinContext)
                is StatInitContext -> visitStatInit(it as StatInitContext)
                is StatStoreContext -> visitStatStore(it as StatStoreContext)
                else -> throw IllegalArgumentException("Unknown StatContext type")
            } as StatAST
        }
    }

    override fun visitProgram(ctx: ProgramContext?): AbstractSyntaxTree
        = ProgramAST(
            functions = ctx?.func()?.map {
                    visitFunc(it) as FunctionAST
                }?.toTypedArray() ?: arrayOf(),
            body = visitStat(ctx?.stat())
        )

    override fun visitFunc(ctx: FuncContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitTypeBaseType(ctx: TypeBaseTypeContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitTypeArrayType(ctx: TypeArrayTypeContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitTypePairType(ctx: TypePairTypeContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitArrayTypeArrayType(ctx: ArrayTypeArrayTypeContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitArrayTypeBaseType(ctx: ArrayTypeBaseTypeContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitArrayTypePairType(ctx: ArrayTypePairTypeContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitArrayElem(ctx: ArrayElemContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitArrayLiterAssignRhs(ctx: ArrayLiterAssignRhsContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitPairLiter(ctx: PairLiterContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitPairElemFst(ctx: PairElemFstContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitPairElemSnd(ctx: PairElemSndContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitPairType(ctx: PairTypeContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitPairElemTypeBaseType(ctx: PairElemTypeBaseTypeContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitPairElemTypeArrayType(ctx: PairElemTypeArrayTypeContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitPairElemTypeKwPair(ctx: PairElemTypeKwPairContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitBaseTypeInt(ctx: BaseTypeIntContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitBaseTypeBool(ctx: BaseTypeBoolContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitBaseTypeChar(ctx: BaseTypeCharContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitBaseTypeString(ctx: BaseTypeStringContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitLiteralInteger(ctx: LiteralIntegerContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitLiteralBoolean(ctx: LiteralBooleanContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitLiteralChar(ctx: LiteralCharContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitLiteralString(ctx: LiteralStringContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitLiteralPair(ctx: LiteralPairContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitExprBoolUnary(ctx: ExprBoolUnaryContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitExprBracket(ctx: ExprBracketContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitExprArrayElem(ctx: ExprArrayElemContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitExprBoolBinary(ctx: ExprBoolBinaryContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitExprCharUnary(ctx: ExprCharUnaryContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitExprIntBinary(ctx: ExprIntBinaryContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitExprIdentifier(ctx: ExprIdentifierContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitExprLiteral(ctx: ExprLiteralContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitExprIntUnary(ctx: ExprIntUnaryContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitAssignLhsExpr(ctx: AssignLhsExprContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitAssignLhsArrayElem(ctx: AssignLhsArrayElemContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitAssignLhsPairElem(ctx: AssignLhsPairElemContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitAssignRhsExpr(ctx: AssignRhsExprContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitAssignRhsArrayLiter(ctx: AssignRhsArrayLiterContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitAssignRhsNewPair(ctx: AssignRhsNewPairContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitAssignRhsPairElem(ctx: AssignRhsPairElemContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitAssignRhsCall(ctx: AssignRhsCallContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitArgList(ctx: ArgListContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitStatInit(ctx: StatInitContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitStatWhileDo(ctx: StatWhileDoContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitStatRead(ctx: StatReadContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitStatBeginEnd(ctx: StatBeginEndContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitStatFree(ctx: StatFreeContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitStatPrint(ctx: StatPrintContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitStatPrintln(ctx: StatPrintlnContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitStatExit(ctx: StatExitContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitStatStore(ctx: StatStoreContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitStatJoin(ctx: StatJoinContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitStatSkip(ctx: StatSkipContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitStatReturn(ctx: StatReturnContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitStatIfThenElse(ctx: StatIfThenElseContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitParam(ctx: ParamContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }

    override fun visitParamList(ctx: ParamListContext?): AbstractSyntaxTree {
        return visitChildren(ctx)
    }
}
