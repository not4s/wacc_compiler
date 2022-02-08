package utils

import waccType.WAny
import org.antlr.v4.runtime.ParserRuleContext
import kotlin.system.exitProcess

class PositionedError(
    private val lineNumber: Int,
    private val columnNumber: Int,
    private val lineText: String,
) {
    override fun toString(): String {
        val linePrefix = "$lineNumber | "
        val arrowLength = 3
        val arrowAlignment = " ".repeat(linePrefix.length + columnNumber)
        val pointingArrow = "$arrowAlignment|\n".repeat(arrowLength - 1) + arrowAlignment + "V\n"
        return "Error at line $lineNumber, position $columnNumber as follows:\n"+
                pointingArrow + "$linePrefix$lineText\n"
    }
}

data class ErrorMessage(
    val prefix: String,
    val start: PositionedError,
    val body: String,
) {
    override fun toString(): String {
        return errorHeader(prefix) + "\n\n" +
                start + "\n\n" +
                body + "\n\n"
    }

    companion object {
        fun errorHeader(prefix: String): String {
            return "----------< $prefix! >----------"
        }
    }
}

/**
 * The class is abstract in order to avoid creating Errors which have
 * semantic nature but syntax error message body and vice versa
 */
abstract class ErrorMessageBuilder {
    protected abstract val prefix: String
    protected var body: String = ""
    protected lateinit var start: PositionedError

    fun build(): ErrorMessage {
        return ErrorMessage(prefix, start, body)
    }

    fun buildAndDisplay() {
        println(build())
    }

    fun provideStart(lineNumber: Int, columnNumber: Int, lineText: String) {
        this.start = PositionedError(lineNumber, columnNumber, lineText)
    }

    fun provideStart(start: PositionedError) {
        this.start = start
    }
}

class SemanticErrorMessageBuilder : ErrorMessageBuilder() {

    override val prefix = "SEMANTIC ERROR"

    fun arrayEntriesTypeClash() {
        body = "The elements of the array have inconsistent types!"
    }

    fun arrayEntriesTypeMismatch(requiredType: WAny, actualType: WAny) {
        body = "The elements of the array of type $requiredType[] have incorrect type $actualType"
    }

    fun nonIntExpressionExit(actualType: WAny) {
        body = "Cannot exit with non-int expression. Actual: Char"
    }

    fun nonArrayTypeElemAccess(nonArrayType: WAny) {
        body = "Cannot access index elements of non-array type: $nonArrayType"
    }

    fun operandTypeMismatch(expectedType: WAny, actualType: WAny, expressionText: String = "") {
        val expression = if (expressionText.isNotEmpty()) "\"$expressionText\"" else ""
        body = "The $actualType expression $expression does not conform to the expected type $expectedType"
    }
}

class SyntaxErrorMessageBuilder : ErrorMessageBuilder() {
    override val prefix = "SYNTAX ERROR"
}

fun main() {
}

fun raiseTypeErrorAndExit(ctx: ParserRuleContext?, expectedType: WAny?, actualType: WAny?) {
    ctx?.run {
        println("Line ${ctx.start.line}: Invalid operand expression type\n" +
                "Expected: $expectedType, got: $actualType\"")
        exitProcess(ExitCode.SEMANTIC_ERROR)
    }
    raiseNullContextError()
}

fun raiseNullContextError() {
    println("Null Context Error")
    exitProcess(ExitCode.UNKNOWN_ERROR)
}

fun raiseSemanticErrorAndExit() {
    println("Semantic error!")
    exitProcess(ExitCode.SEMANTIC_ERROR)
}

class SemanticException(private val reason: String) : Exception() {
    override val message: String
        get() = "Semantic error!\n$reason"
}