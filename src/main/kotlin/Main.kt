import antlr.BasicLexer
import antlr.BasicParser
import org.antlr.v4.runtime.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    println("You have passed in: ${args.joinToString()}")
    val file = System.getProperty("user.dir") + "/" + args[0]
    println("Opening file: $file\n")

    val input = CharStreams.fromFileName(file)

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

    val tree = parser.prog()

    println("Parsed: ${tree.toStringTree(parser)}")

}

class TerminateOnErrorStrategy : DefaultErrorStrategy() {
    override fun reportError(recognizer: Parser?, e: RecognitionException?) {
        println(e)
        exitProcess(100)
    }
}
