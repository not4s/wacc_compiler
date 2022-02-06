import antlr.WACCLexer
import antlr.WACCParser
import antlr.WACCParser.AssignRhsExprContext
import antlr.WACCParserBaseVisitor
import org.antlr.v4.runtime.*
import semantic.ExprVisitor
import utils.Debug
import utils.ExitCode
import java.io.File
import javax.swing.DebugGraphics
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    // Setting debug mode for future logs
    if (args.contains(Debug.FLAG_ARG)) {
        Debug.isInDebugMode = true
    }
    Debug.infoLog("Debug mode is on!")
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
            exitProcess(ExitCode.SYNTAX_ERROR)
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
        exitProcess(ExitCode.SYNTAX_ERROR)
    }
}

class CustomVisitor : WACCParserBaseVisitor<Void?>() {

    override fun visitAssignRhsExpr(ctx: AssignRhsExprContext?): Void? {
        ExprVisitor().visit(ctx)
        return null
    }

    override fun visitStatPrintln(ctx: WACCParser.StatPrintlnContext?): Void? {
        ctx?.apply { ExprVisitor().visit(expr()) }
        return null
    }

    override fun visitStatPrint(ctx: WACCParser.StatPrintContext?): Void? {
        ctx?.apply { ExprVisitor().visit(expr()) }
        return null
    }

    override fun visitLiteralInteger(ctx: WACCParser.LiteralIntegerContext?): Void? {
        // Check if int is within limits
        try {
            Integer.parseInt(ctx?.text)
        } catch (e: java.lang.NumberFormatException) {
            exitProcess(ExitCode.SYNTAX_ERROR)
        }
        return null
    }
}
