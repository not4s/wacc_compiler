from antlr4 import *
from antlr.WACCLexer import WACCLexer
from antlr.WACCParser import WACCParser
from antlr.WACCParserVisitor import WACCParserVisitor


class PainterVisitor(WACCParserVisitor):
    ''' Visits the nodes which are relevant for painting, i.e. terminals '''

    def __init__(self):
        self.commands = []

    # Visit a parse tree produced by WACCParser#program.
    def visitProgram(self, ctx:WACCParser.ProgramContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#typeBaseType.
    def visitTypeBaseType(self, ctx:WACCParser.TypeBaseTypeContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#typeArrayType.
    def visitTypeArrayType(self, ctx:WACCParser.TypeArrayTypeContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#typePairType.
    def visitTypePairType(self, ctx:WACCParser.TypePairTypeContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#arrayTypeArrayType.
    def visitArrayTypeArrayType(self, ctx:WACCParser.ArrayTypeArrayTypeContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#arrayTypeBaseType.
    def visitArrayTypeBaseType(self, ctx:WACCParser.ArrayTypeBaseTypeContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#arrayTypePairType.
    def visitArrayTypePairType(self, ctx:WACCParser.ArrayTypePairTypeContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#arrayElem.
    def visitArrayElem(self, ctx:WACCParser.ArrayElemContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#arrayLiterAssignRhs.
    def visitArrayLiterAssignRhs(self, ctx:WACCParser.ArrayLiterAssignRhsContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#pairElemFst.
    def visitPairElemFst(self, ctx:WACCParser.PairElemFstContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#pairElemSnd.
    def visitPairElemSnd(self, ctx:WACCParser.PairElemSndContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#pairType.
    def visitPairType(self, ctx:WACCParser.PairTypeContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#pairElemTypeBaseType.
    def visitPairElemTypeBaseType(self, ctx:WACCParser.PairElemTypeBaseTypeContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#pairElemTypeArrayType.
    def visitPairElemTypeArrayType(self, ctx:WACCParser.PairElemTypeArrayTypeContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#pairElemTypeKwPair.
    def visitPairElemTypeKwPair(self, ctx:WACCParser.PairElemTypeKwPairContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#baseTypeInt.
    def visitBaseTypeInt(self, ctx:WACCParser.BaseTypeIntContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#baseTypeBool.
    def visitBaseTypeBool(self, ctx:WACCParser.BaseTypeBoolContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#baseTypeChar.
    def visitBaseTypeChar(self, ctx:WACCParser.BaseTypeCharContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#baseTypeString.
    def visitBaseTypeString(self, ctx:WACCParser.BaseTypeStringContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#literalInteger.
    def visitLiteralInteger(self, ctx:WACCParser.LiteralIntegerContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#literalBoolean.
    def visitLiteralBoolean(self, ctx:WACCParser.LiteralBooleanContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#literalChar.
    def visitLiteralChar(self, ctx:WACCParser.LiteralCharContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#literalString.
    def visitLiteralString(self, ctx:WACCParser.LiteralStringContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#literalPair.
    def visitLiteralPair(self, ctx:WACCParser.LiteralPairContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#exprBracket.
    def visitExprBracket(self, ctx:WACCParser.ExprBracketContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#exprBinary.
    def visitExprBinary(self, ctx:WACCParser.ExprBinaryContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#exprArrayElem.
    def visitExprArrayElem(self, ctx:WACCParser.ExprArrayElemContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#exprIdentifier.
    def visitExprIdentifier(self, ctx:WACCParser.ExprIdentifierContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#exprUnary.
    def visitExprUnary(self, ctx:WACCParser.ExprUnaryContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#exprLiteral.
    def visitExprLiteral(self, ctx:WACCParser.ExprLiteralContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#assignLhsExpr.
    def visitAssignLhsExpr(self, ctx:WACCParser.AssignLhsExprContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#assignLhsArrayElem.
    def visitAssignLhsArrayElem(self, ctx:WACCParser.AssignLhsArrayElemContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#assignLhsPairElem.
    def visitAssignLhsPairElem(self, ctx:WACCParser.AssignLhsPairElemContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#assignRhsExpr.
    def visitAssignRhsExpr(self, ctx:WACCParser.AssignRhsExprContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#assignRhsArrayLiter.
    def visitAssignRhsArrayLiter(self, ctx:WACCParser.AssignRhsArrayLiterContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#assignRhsNewPair.
    def visitAssignRhsNewPair(self, ctx:WACCParser.AssignRhsNewPairContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#assignRhsPairElem.
    def visitAssignRhsPairElem(self, ctx:WACCParser.AssignRhsPairElemContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#assignRhsCall.
    def visitAssignRhsCall(self, ctx:WACCParser.AssignRhsCallContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#argList.
    def visitArgList(self, ctx:WACCParser.ArgListContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#statInit.
    def visitStatInit(self, ctx:WACCParser.StatInitContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#statWhileDo.
    def visitStatWhileDo(self, ctx:WACCParser.StatWhileDoContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#statRead.
    def visitStatRead(self, ctx:WACCParser.StatReadContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#statBeginEnd.
    def visitStatBeginEnd(self, ctx:WACCParser.StatBeginEndContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#statFree.
    def visitStatFree(self, ctx:WACCParser.StatFreeContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#statPrint.
    def visitStatPrint(self, ctx:WACCParser.StatPrintContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#statPrintln.
    def visitStatPrintln(self, ctx:WACCParser.StatPrintlnContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#statExit.
    def visitStatExit(self, ctx:WACCParser.StatExitContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#statStore.
    def visitStatStore(self, ctx:WACCParser.StatStoreContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#statJoin.
    def visitStatJoin(self, ctx:WACCParser.StatJoinContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#statSkip.
    def visitStatSkip(self, ctx:WACCParser.StatSkipContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#statReturn.
    def visitStatReturn(self, ctx:WACCParser.StatReturnContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#statIfThenElse.
    def visitStatIfThenElse(self, ctx:WACCParser.StatIfThenElseContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#param.
    def visitParam(self, ctx:WACCParser.ParamContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#paramList.
    def visitParamList(self, ctx:WACCParser.ParamListContext):
        return self.visitChildren(ctx)


    # Visit a parse tree produced by WACCParser#func.
    def visitFunc(self, ctx:WACCParser.FuncContext):
        return self.visitChildren(ctx)


class Painter:
    ''' Works with ANTLR Parser and CodeText to create syntax highlight '''

    def __init__(self, code_text):
        self.text = code_text

    def paint(self):
        '''

        "Compiling" the code and getting the sequence of commands to color the code
        command is a triple which contains tag name, start, end
        for example:
            ('keyword', '5.0', '6.0')

        painting_commands :: [(tag :: str, start :: str, end :: str)]

        '''
        lexer = WACCLexer(InputStream(self.text.get("1.0", "end")))
        tokens = CommonTokenStream(lexer)
        parser = WACCParser(tokens)
        tree = parser.program()

        visitor = PainterVisitor()
        visitor.visit(tree)

        painting_commands = visitor.commands
        print(painting_commands)

        map(lambda cmd: self.text.tag_add(*cmd), painting_commands)
