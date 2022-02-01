import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.*
import antlr.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    println("You have passed in: ${args.joinToString()}")
    println("Hello world! The following is a test to make sure BasicLexer works.")

    val input = CharStreams.fromString("1+2+(3+4)")

    val lexer = BasicLexer(input)

    val tokens = CommonTokenStream(lexer)

    val parser = BasicParser(tokens)

    val tree = parser.prog()

    println(tree.toStringTree(parser))

    println()
    println("If the argument passed in contains \'invalid\', this code will exit with 42.")
    if (args[0].contains("invalid")) {
        exitProcess(42)
    } else {
        exitProcess(0)
    }
}