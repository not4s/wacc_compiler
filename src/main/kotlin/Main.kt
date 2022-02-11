import antlr.WACCLexer
import antlr.WACCParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import semantic.ASTVisitor
import symbolTable.ParentRefSymbolTable
import syntax.SyntaxErrBuilderErrorListener
import utils.ExitCode
import utils.SemanticException
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("Please provide filepath as argument.")
    }
    println("You have passed in: ${args.joinToString()}")
    val file = File(args[0])
    println("Opening file: $file\n")

    val input = CharStreams.fromFileName(file.absolutePath)

    val lexer = WACCLexer(input)

    val tokens = CommonTokenStream(lexer)

    val parser = WACCParser(tokens)

    // setting the only listeners to our custom listener
    parser.removeErrorListeners()
    parser.addErrorListener(SyntaxErrBuilderErrorListener(file))

    val tree = parser.program()
    try {
        val res = ASTVisitor(ParentRefSymbolTable(file.absolutePath)).visit(tree)
        println(res)
    } catch (e: SemanticException) {
        println(e.reason)
        exitProcess(ExitCode.SEMANTIC_ERROR)
    }

}