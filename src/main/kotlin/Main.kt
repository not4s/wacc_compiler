import antlr.WACCLexer
import antlr.WACCParser
import org.antlr.v4.runtime.*
import semantic.ASTVisitor
import symbolTable.ParentRefSymbolTable
import utils.ExitCode
import utils.SemanticException
import utils.SyntaxErrorMessageBuilder
import waccType.WArray
import waccType.WInt
import waccType.WStr
import waccType.typesAreEqual
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    println("You have passed in: ${args.joinToString()}")
    val file =
        File(args.getOrNull(0) ?: "wacc_test/sample_programs/invalid/semanticErr/array/arrayTypeClash.wacc")
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
                .provideStart(line, charPositionInLine, getErrorLine(line))
                .appendCustomErrorMessage(msg!!)
                .buildAndPrint()
            exitProcess(ExitCode.SYNTAX_ERROR)
        }

        fun getErrorLine(line: Int): String = input.toString().split("\n")[line - 1]
    })

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