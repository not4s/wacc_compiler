import antlr.BasicLexer
import antlr.BasicParser
import antlr.BasicParserBaseVisitor
import org.antlr.v4.runtime.*
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    println("You have passed in: ${args.joinToString()}")
    val file = File(args[0])
    println("Opening file: $file\n")

    val input = CharStreams.fromFileName(file.absolutePath)

    val lexer = BasicLexer(input)

    val tokens = CommonTokenStream(lexer)

    val parser = BasicParser(tokens)

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

class CustomVisitor : BasicParserBaseVisitor<Void>() {
    override fun visitIntegerLiteral(ctx: BasicParser.IntegerLiteralContext?): Void? {
        // Check if int is within limits
        try {
            val integer: Int = Integer.parseInt(ctx?.text)
        } catch (e: java.lang.NumberFormatException) {
            exitProcess(100)
        }
        return null
    }
}
