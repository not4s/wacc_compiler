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

class ErrorMessageBuilder {

    private var prefix: String = ""
    private var body: String = ""
    private lateinit var start: PositionedError

    companion object {
        const val SEMANTIC_ERROR = "SEMANTIC ERROR"
        const val SYNTAX_ERROR = "SYNTAX ERROR"
    }

    fun build(): ErrorMessage {
        return ErrorMessage(prefix, start, body)
    }

    fun operandTypeMismatch(expectedType: WAny, actualType: WAny, expressionText: String = "") {
        val expression = if (expressionText.isNotEmpty()) "\"$expressionText\"" else ""
        body = "The $actualType expression $expression does not conform to the expected type $expectedType"
    }
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