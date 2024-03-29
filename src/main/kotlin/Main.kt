import antlr.WACCLexer
import antlr.WACCParser
import ast.ProgramAST
import codegen.ProgramVisitor
import codegen.WInstrToString.Companion.translateInstructions
import codegen.InstructionEvaluation.Companion.evaluateInstructions
import instructions.misc.DataDeclaration
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import semantic.ASTProducer
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
    val file = File(args[0])

    val input = CharStreams.fromFileName(file.absolutePath)

    val lexer = WACCLexer(input)

    val tokens = CommonTokenStream(lexer)

    val parser = WACCParser(tokens)

    // setting the only listeners to our custom listener
    parser.removeErrorListeners()
    parser.addErrorListener(SyntaxErrBuilderErrorListener(file))

    val tree = parser.program()
    val ast: ProgramAST
    try {
        ast = ASTProducer(ParentRefSymbolTable(file.absolutePath)).visit(tree) as ProgramAST
    } catch (e: SemanticException) {
        println(e.reason)
        exitProcess(ExitCode.SEMANTIC_ERROR)
    }
    val instructions = ProgramVisitor(DataDeclaration()).visit(ast)
    val optimised_instructions = evaluateInstructions(instructions)
    val code = translateInstructions(optimised_instructions)
    println(code)
}