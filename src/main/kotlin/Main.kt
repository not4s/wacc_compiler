import antlr.WACCLexer
import antlr.WACCParser
import antlr.WACCParserBaseVisitor
import org.antlr.v4.runtime.*
import semantic.ASTVisitor
import symbolTable.ParentRefSymbolTable
import utils.ExitCode
import utils.SemanticException
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    println("You have passed in: ${args.joinToString()}")
    val file = File(args[0])
    println("Opening file: $file\n")

    val input = CharStreams.fromFileName(file.absolutePath)
//    val input = CharStreams.fromString(
//        """
//        begin
//            int x = 5;
//            int y = "hi";
//            exit 2
//        end
//
//    """.trimIndent()
//    )

    val lexer = WACCLexer(input)

    val tokens = CommonTokenStream(lexer)

    val parser = WACCParser(tokens)

    parser.removeErrorListeners()
    parser.addErrorListener(object : BaseErrorListener() {
        override fun syntaxError(
            recognizer: Recognizer<*, *>?,
            offendingSymbol: Any?,
            line: Int,
            charPositionInLine: Int,
            msg: String?,
            e: RecognitionException?
        ) {
            println(msg)
            exitProcess(ExitCode.SYNTAX_ERROR)
        }
    })

    parser.errorHandler = TerminateOnErrorStrategy()

    val tree = parser.program()
    try {
        val res = ASTVisitor(ParentRefSymbolTable()).visit(tree)
        println(res)
    } catch (e: SemanticException) {
        println("-----------SEMANTIC ERROR-----------")
        println(e.message)
        exitProcess(ExitCode.SEMANTIC_ERROR)
    }

}

class TerminateOnErrorStrategy : DefaultErrorStrategy() {
    override fun reportError(recognizer: Parser?, e: RecognitionException?) {
        println(e)
        exitProcess(ExitCode.SYNTAX_ERROR)
    }
}

class CustomVisitor : WACCParserBaseVisitor<Void>() {
    override fun visitLiteralInteger(ctx: WACCParser.LiteralIntegerContext?): Void? {
        // Check if int is within limits
        try {
            val integer: Int = Integer.parseInt(ctx?.text)
        } catch (e: java.lang.NumberFormatException) {
            exitProcess(ExitCode.SYNTAX_ERROR)
        }
        return null
    }
}
