import antlr.WACCLexer
import antlr.WACCParser
import antlr.WACCParserBaseVisitor
import ast.*
import com.sun.source.tree.LiteralTree
import org.antlr.v4.runtime.*
import org.junit.*
import kotlin.test.assertEquals
import kotlin.test.fail
import waccType.*

internal class ASTTests() {
    
    @Test
    fun canParseFunc() {

    }

    @Test
    fun canParseBaseType() {

    }

    @Test
    fun canParseTypeArrayType() {

    }

    @Test
    fun canParseTypePairType() {

    }

    @Test
    fun canParseArrayTypeArrayType() {

    }

    @Test
    fun canParseArrayTypeBaseType() {

    }

    @Test
    fun canParseArrayTypePairType() {

    }

    @Test
    fun canParseArrayElem() {

    }

    @Test
    fun canParseArrayLiterAssignRhs() {

    }

    @Test
    fun canParsePairLiter() {

    }

    @Test
    fun canParsePairElemFst() {

    }

    @Test
    fun canParsePairElemSnd() {

    }

    @Test
    fun canParsePairType() {

    }

    @Test
    fun canParsePairElemTypeBaseType() {

    }

    @Test
    fun canParsePairElemTypeArrayType() {

    }

    @Test
    fun canParsePairElemTypeKwPair() {

    }

    @Test
    fun canParseBaseTypeInt() {

    }

    @Test
    fun canParseBaseTypeBool() {

    }

    @Test
    fun canParseBaseTypeChar() {

    }

    @Test
    fun canParseBaseTypeString() {

    }

    @Test
    fun canParseLiteralInteger() {

    }

    @Test
    fun canParseLiteralBoolean() {

    }

    @Test
    fun canParseLiteralChar() {

    }

    @Test
    fun canParseLiteralString() {

    }

    @Test
    fun canParseLiteralPair() {

    }

    @Test
    fun canParseExprBoolUnary() {

    }

    @Test
    fun canParseExprBracket() {

    }

    @Test
    fun canParseExprArrayElem() {

    }

    @Test
    fun canParseExprBoolBinary() {

    }

    @Test
    fun canParseExprCharUnary() {

    }

    @Test
    fun canParseExprIntBinary() {
        val input = CharStreams.fromString("1 + 1")
        val lexer = WACCLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = WACCParser(tokens)

        val tree = parser.expr()
        val result: AbstractSyntaxTree = AstProducerVisitor().visit(tree)

        val desiredOutput
        = BinOpAST(LiteralAST(WInt(1)), BinOpAST.BinOperator.ADD, LiteralAST(WInt(1)))

        assertEquals(desiredOutput, result)
    }

    @Test
    fun canParseExprIdentifier() {

    }

    @Test
    fun canParseExprLiteral() {

    }

    @Test
    fun canParseExprIntUnary() {

    }

    @Test
    fun canParseAssignLhsExpr() {

    }

    @Test
    fun canParseAssignLhsArrayElem() {

    }

    @Test
    fun canParseAssignLhsPairElem() {

    }

    @Test
    fun canParseAssignRhsExpr() {

    }

    @Test
    fun canParseAssignLRhsArrayLiter() {

    }

    @Test
    fun canParseAssignRhsNewPair() {

    }

    @Test
    fun canParseAssignRhsPairElem() {

    }

    @Test
    fun canParseAssignRhsCall() {

    }

    @Test
    fun canParseArgList() {

    }

    @Test
    fun canParseStatInit() {

    }

    @Test
    fun canParseStatWhileDo() {

    }

    @Test
    fun canParseStatRead() {

    }

    @Test
    fun canParseStatBeginEnd() {

    }

    @Test
    fun canParseStatFree() {

    }

    @Test
    fun canParseStatPrint() {

    }

    @Test
    fun canParseStatPrintln() {

    }

    @Test
    fun canParseStatExit() {

    }

    @Test
    fun canParseStatStore() {

    }

    @Test
    fun canParseStatJoin() {

    }

    @Test
    fun canParseStatSkip() {

    }

    @Test
    fun canParseStatReturn() {

    }

    @Test
    fun canParseStatIfThenElse() {

    }

    @Test
    fun canParseParam() {

    }

    @Test
    fun canParseParamList() {

    }
}