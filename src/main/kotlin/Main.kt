import antlr.WACCLexer
import antlr.WACCParser
import antlr.WACCParserBaseVisitor
import org.antlr.v4.runtime.*
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    println("You have passed in: ${args.joinToString()}")
    val file = File(args[0])
    println("Opening file: $file\n")

    val input = CharStreams.fromFileName(file.absolutePath)

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
            exitProcess(100)
        }
    })

    parser.errorHandler = TerminateOnErrorStrategy()

    val tree = parser.program()
    CustomVisitor().visit(tree)

    println("Parsed: ${tree.toStringTree(parser)}")

}

class TerminateOnErrorStrategy : DefaultErrorStrategy() {
    override fun reportError(recognizer: Parser?, e: RecognitionException?) {
        println(e)
        exitProcess(100)
    }
}

class CustomVisitor : WACCParserBaseVisitor<Void>() {
    override fun visitIntegerLiteral(ctx: WACCParser.IntegerLiteralContext?): Void? {
        // Check if int is within limits
        try {
            val integer: Int = Integer.parseInt(ctx?.text)
        } catch (e: java.lang.NumberFormatException) {
            exitProcess(100)
        }
        return null
    }
}
