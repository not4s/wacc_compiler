from antlr4 import *
from antlr.WACCLexer import WACCLexer
from antlr.WACCParser import WACCParser
from antlr.WACCParserVisitor import WACCParserVisitor
from io import StringIO
from eventlog import SyntaxErrorListener


class PainterVisitor(WACCParserVisitor):
    ''' Visits the nodes which are relevant for painting, i.e. terminals '''

    def __init__(self):
        self.painting_commands = []

    def paint_token(self, token, tag):
        ''' Associating tag with token start and end coordinates '''

        if (token is None):
            return
        try:
            actual_token = token.getSymbol()
        except AttributeError:
            actual_token = token

        line = actual_token.line
        start = actual_token.start
        stop = actual_token.stop
        begin_column = actual_token.column
        end_column = begin_column + stop - start + 1

        start_coord = f"{line}.{begin_column}"
        end_coord = f"{line}.{end_column}"
        self.painting_commands.append((tag, start_coord, end_coord))

    def paint_keyword(self, token):
        self.paint_token(token, "keyword")

    def paint_main_text(self, token):
        self.paint_token(token, "main text")

    def paint_string(self, token):
        self.paint_token(token, "string")

    def paint_int(self, token):
        self.paint_token(token, "int")

    def paint_type(self, token):
        self.paint_token(token, "type")

    def paint_operator(self, token):
        self.paint_token(token, "operator")

    def paint_declaration(self, token):
        self.paint_token(token, "declaration")

    def paint_function(self, token):
        self.paint_token(token, "function")

    def paint_attribute(self, token):
        self.paint_token(token, "attribute")

    def paint_base_type(self, type_ctx):
        if isinstance(type_ctx, WACCParser.BaseTypeIntContext):
            self.paint_type(type_ctx.KW_INT())
        elif isinstance(type_ctx, WACCParser.BaseTypeBoolContext):
            self.paint_type(type_ctx.KW_BOOL())
        elif isinstance(type_ctx, WACCParser.BaseTypeCharContext):
            self.paint_type(type_ctx.KW_CHAR())
        elif isinstance(type_ctx, WACCParser.BaseTypeStringContext):
            self.paint_type(type_ctx.KW_STRING())
        else:
            raise TypeError(f"Incorrect type context {type(type_ctx)}")

    def paint_array_type(self, type_ctx):
        if isinstance(type_ctx, WACCParser.ArrayTypeBaseTypeContext):
            self.paint_base_type(type_ctx.baseType())
        elif isinstance(type_ctx, WACCParser.ArrayTypeArrayTypeContext):
            self.paint_array_type(type_ctx.arrayType())
        elif isinstance(type_ctx, WACCParser.ArrayTypePairTypeContext):
            self.paint_pair_type(type_ctx.pairType())
        else:
            raise TypeError(f"Incorrect type context {type(type_ctx)}")

    def paint_pair_type(self, type_ctx):
        self.paint_type(type_ctx.KW_PAIR())
        for elem in [type_ctx.left, type_ctx.right]:
            if isinstance(elem, WACCParser.PairElemTypeBaseTypeContext):
                self.paint_base_type(elem.baseType())
            elif isinstance(elem, WACCParser.PairElemTypeArrayTypeContext):
                self.paint_array_type(elem.arrayType())
            elif isinstance(elem, WACCParser.PairElemTypeKwPairContext):
                self.paint_type(elem.KW_PAIR())
            else:
                raise TypeError(f"Incorrect type context {type(elem)}")

    def visitProgram(self, ctx:WACCParser.ProgramContext):
        self.paint_keyword(ctx.KW_BEGIN())
        self.paint_keyword(ctx.KW_END())
        return self.visitChildren(ctx)

    def visitTypeBaseType(self, ctx:WACCParser.TypeBaseTypeContext):
        self.paint_base_type(ctx.baseType())
        return self.visitChildren(ctx)

    def visitTypeArrayType(self, ctx:WACCParser.TypeArrayTypeContext):
        self.paint_array_type(ctx.arrayType())
        return self.visitChildren(ctx)

    def visitTypePairType(self, ctx:WACCParser.TypePairTypeContext):
        self.paint_pair_type(ctx.pairType())
        return self.visitChildren(ctx)

    def visitArrayLiterAssignRhs(self, ctx:WACCParser.ArrayLiterAssignRhsContext):
        for comma in ctx.SYM_COMMA():
            self.paint_keyword(comma)
        return self.visitChildren(ctx)

    def visitPairElemFst(self, ctx:WACCParser.PairElemFstContext):
        self.paint_operator(ctx.KW_FST())
        return self.visitChildren(ctx)

    def visitPairElemSnd(self, ctx:WACCParser.PairElemSndContext):
        self.paint_operator(ctx.KW_SND())
        return self.visitChildren(ctx)

    def visitPairType(self, ctx:WACCParser.PairTypeContext):
        self.paint_type(ctx.KW_PAIR())
        return self.visitChildren(ctx)

    def visitPairElemTypeBaseType(self, ctx:WACCParser.PairElemTypeBaseTypeContext):
        self.paint_base_type(ctx.baseType())
        return self.visitChildren(ctx)

    def visitPairElemTypeArrayType(self, ctx:WACCParser.PairElemTypeArrayTypeContext):
        self.paint_array_type(ctx.arrayType())
        return self.visitChildren(ctx)

    def visitPairElemTypeKwPair(self, ctx:WACCParser.PairElemTypeKwPairContext):
        self.paint_type(ctx.KW_PAIR())
        return self.visitChildren(ctx)

    def visitLiteralInteger(self, ctx:WACCParser.LiteralIntegerContext):
        self.paint_int(ctx.INTEGER())
        return self.visitChildren(ctx)

    def visitLiteralBoolean(self, ctx:WACCParser.LiteralBooleanContext):
        self.paint_int(ctx.value)
        return self.visitChildren(ctx)

    def visitLiteralChar(self, ctx:WACCParser.LiteralCharContext):
        self.paint_string(ctx.CHAR())
        return self.visitChildren(ctx)

    def visitLiteralString(self, ctx:WACCParser.LiteralStringContext):
        self.paint_string(ctx.STRING())
        return self.visitChildren(ctx)

    def visitLiteralPair(self, ctx:WACCParser.LiteralPairContext):
        self.paint_keyword(ctx.KW_NULL())
        return self.visitChildren(ctx)

    def visitExprBinary(self, ctx:WACCParser.ExprBinaryContext):
        self.paint_operator(ctx.binOp)
        return self.visitChildren(ctx)

    def visitExprUnary(self, ctx:WACCParser.ExprUnaryContext):
        self.paint_operator(ctx.unOp)
        return self.visitChildren(ctx)

    def visitAssignRhsNewPair(self, ctx:WACCParser.AssignRhsNewPairContext):
        self.paint_keyword(ctx.KW_NEWPAIR())
        return self.visitChildren(ctx)

    def visitAssignRhsCall(self, ctx:WACCParser.AssignRhsCallContext):
        self.paint_keyword(ctx.KW_CALL())
        self.paint_function(ctx.IDENTIFIER())
        return self.visitChildren(ctx)

    def visitStatWhileDo(self, ctx:WACCParser.StatWhileDoContext):
        self.paint_keyword(ctx.KW_WHILE())
        self.paint_keyword(ctx.KW_DO())
        self.paint_keyword(ctx.KW_DONE())
        return self.visitChildren(ctx)

    def visitStatInit(self, ctx:WACCParser.StatInitContext):
        self.paint_operator(ctx.SYM_EQUALS())
        return self.visitChildren(ctx)

    def visitStatStore(self, ctx:WACCParser.StatStoreContext):
        self.paint_operator(ctx.SYM_EQUALS())
        return self.visitChildren(ctx)

    def visitStatRead(self, ctx:WACCParser.StatReadContext):
        self.paint_keyword(ctx.KW_READ())
        return self.visitChildren(ctx)

    def visitStatBeginEnd(self, ctx:WACCParser.StatBeginEndContext):
        self.paint_keyword(ctx.KW_BEGIN())
        self.paint_keyword(ctx.KW_END())
        return self.visitChildren(ctx)

    def visitStatFree(self, ctx:WACCParser.StatFreeContext):
        self.paint_keyword(ctx.KW_FREE())
        return self.visitChildren(ctx)

    def visitStatPrint(self, ctx:WACCParser.StatPrintContext):
        self.paint_keyword(ctx.KW_PRINT())
        return self.visitChildren(ctx)

    def visitStatPrintln(self, ctx:WACCParser.StatPrintlnContext):
        self.paint_keyword(ctx.KW_PRINTLN())
        return self.visitChildren(ctx)

    def visitStatExit(self, ctx:WACCParser.StatExitContext):
        self.paint_keyword(ctx.KW_EXIT())
        return self.visitChildren(ctx)

    def visitStatJoin(self, ctx:WACCParser.StatJoinContext):
        self.paint_function(ctx.SYM_SEMICOLON())
        return self.visitChildren(ctx)

    def visitStatSkip(self, ctx:WACCParser.StatSkipContext):
        self.paint_keyword(ctx.KW_SKIP())
        return self.visitChildren(ctx)

    def visitStatReturn(self, ctx:WACCParser.StatReturnContext):
        self.paint_keyword(ctx.KW_RETURN())
        return self.visitChildren(ctx)

    def visitStatIfThenElse(self, ctx:WACCParser.StatIfThenElseContext):
        self.paint_keyword(ctx.KW_IF())
        self.paint_keyword(ctx.KW_THEN())
        self.paint_keyword(ctx.KW_ELSE())
        self.paint_keyword(ctx.KW_FI())
        return self.visitChildren(ctx)

    def visitParamList(self, ctx:WACCParser.ParamListContext):
        for comma in ctx.SYM_COMMA():
            self.paint_function(comma)
        return self.visitChildren(ctx)

    def visitFunc(self, ctx:WACCParser.FuncContext):
        self.paint_keyword(ctx.KW_IS())
        self.paint_keyword(ctx.KW_END())
        self.paint_declaration(ctx.IDENTIFIER())
        return self.visitChildren(ctx)

    ### Struct feature methods:

    def visitStruct(self, ctx:WACCParser.StructContext):
        self.paint_keyword(ctx.KW_STRUCT())
        self.paint_keyword(ctx.KW_BEGIN())
        self.paint_keyword(ctx.KW_END())
        self.paint_declaration(ctx.IDENTIFIER())
        return self.visitChildren(ctx)

    def visitStructType(self, ctx:WACCParser.StructTypeContext):
        self.paint_keyword(ctx.KW_STRUCT())
        self.paint_type(ctx.IDENTIFIER())
        return self.visitChildren(ctx)

    def visitStructElem(self, ctx:WACCParser.StructElemContext):
        for period in ctx.SYM_PERIOD():
            self.paint_operator(period)
        for elem in ctx.IDENTIFIER()[1:]:
            self.paint_attribute(elem)
        return self.visitChildren(ctx)

    def visitStructElems(self, ctx:WACCParser.StructElemsContext):
        for semicolon in ctx.SYM_SEMICOLON():
            self.paint_function(semicolon)
        return self.visitChildren(ctx)


class Painter:
    ''' Works with ANTLR and CodeText to create syntax highlight
        Also delegates With error highlighting '''

    def __init__(self, code_text):
        self.text = code_text
        self.event_log = None
        self.errors = []

    def paint_comments(self, text):
        line_counter = 0
        for line in StringIO(text):
            line_counter += 1
            if '#' not in line:
                continue
            comment_start = line.find('#')
            self.text.tag_add("comment", f'{line_counter}.{comment_start}', f'{line_counter}.end')

    def paint(self):
        '''
        "Compiling" the code and getting the sequence of commands to color the code
        command is a triple which contains tag name, start, end
        for example:
            ('keyword', '5.0', '6.0')

        visitor.painting_commands :: [(tag :: str, start :: str, end :: str)] '''

        text_content = self.text.get("1.0", "end")
        self.paint_comments(text_content)

        lexer = WACCLexer(InputStream(text_content))
        tokens = CommonTokenStream(lexer)

        parser = WACCParser(tokens)

        parser.removeErrorListeners()
        errorListener = SyntaxErrorListener(self.errors)
        parser.addErrorListener(errorListener)

        tree = parser.program()

        visitor = PainterVisitor()
        visitor.visit(tree)

        self.pass_errors_to_event_log()

        for command in visitor.painting_commands:
            self.text.tag_add(*command)

    def attach_event_log(self, event_log):
        self.event_log = event_log

    def paint_errors(self):
        self.text.tag_remove('error', '1.0', 'end')
        self.text.clear_error_bulbs()
        for er in self.errors:
            self.text.tag_add('error', er.char_pos(),
                              f"{er.line}.{er.charPositionInLine + 1}")
            self.text.add_error_bulb(er)


    def pass_errors_to_event_log(self):
        self.event_log.log(self.errors)
        self.paint_errors()
        self.errors = []
