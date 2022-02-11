import antlr.WACCLexer
import antlr.WACCParser
import org.antlr.v4.runtime.*
import semantic.ASTVisitor
import symbolTable.ParentRefSymbolTable
import utils.ExitCode
import utils.SemanticException
import utils.SyntaxErrorMessageBuilder
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    println("You have passed in: ${args.joinToString()}")
    val file = File(args.getOrNull(0)
        ?: "wacc_test/sample_programs/invalid/semanticErr/array/arrayTypeClash.wacc")
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

    // setting the only listeners to our custom listener
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
            SyntaxErrorMessageBuilder()
                .provideStart(line - 1, charPositionInLine)
                .setLineTextFromSrcFile(file.absolutePath)
                .appendCustomErrorMessage(msg ?: "Null message")
                .buildAndPrint()
            exitProcess(ExitCode.SYNTAX_ERROR)
        }
    })

    val tree = parser.program()
    try {
        val res = ASTVisitor(ParentRefSymbolTable(file.absolutePath)).visit(tree)
        println(res)
    } catch (e: SemanticException) {
        println(e.reason)
        exitProcess(ExitCode.SEMANTIC_ERROR)
    }

}