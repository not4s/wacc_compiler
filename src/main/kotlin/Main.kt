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

}