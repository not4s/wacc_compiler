package utils

import waccType.WAny

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

    fun buildAndDisplay(): ErrorMessageBuilder {
        println(build())
        return this
    }

    fun provideStart(lineNumber: Int, columnNumber: Int, lineText: String): ErrorMessageBuilder {
        this.start = PositionedError(lineNumber, columnNumber, lineText)
        return this
    }

    fun provideStart(start: PositionedError): ErrorMessageBuilder {
        this.start = start
        return this
    }
}

class SemanticErrorMessageBuilder : ErrorMessageBuilder() {

    override val prefix = "SEMANTIC ERROR"

    fun readTypeIsIncorrect(actualType: WAny): SemanticErrorMessageBuilder {
        body += "Cannot read into the variable of type $actualType. Variables must be characters or numeric."
        return this
    }

    fun whileStatConditionHasNotBooleanType(actualType: WAny): SemanticErrorMessageBuilder {
        whileStatConditionHasNotBooleanType()
        body += " Got $actualType instead"
        return this
    }

    fun whileStatConditionHasNotBooleanType(): SemanticErrorMessageBuilder {
        body += "The conditional statement in the \"while\" statement does not evaluate to the boolean type."
        return this
    }

    fun ifStatConditionHasNotBooleanType(actualType: WAny): SemanticErrorMessageBuilder {
        ifStatConditionHasNotBooleanType()
        body += " Got $actualType instead"
        return this
    }

    fun ifStatConditionHasNotBooleanType(): SemanticErrorMessageBuilder {
        body += "The conditional statement in the \"if\" statement does not evaluate to the boolean type."
        return this
    }

    fun variableNotInScope(identifier: String): SemanticErrorMessageBuilder {
        body += "The variable $identifier is undefined in the scope it is accessed or any parent scope"
        return this
    }

    fun functionArgumentTypeMismatch(expectedType: WAny, actualType: WAny): SemanticErrorMessageBuilder {
        body += "The supplied function argument has incorrect type. Expected $expectedType, got $actualType instead."
        return this
    }

    fun functionRedefineError(functionName: String): SemanticErrorMessageBuilder {
        body += "Cannot redefine function $functionName"
        return this
    }

    fun functionRedefineError(): SemanticErrorMessageBuilder {
        body += "Cannot redefine function"
        return this
    }

    fun functionArgumentCountMismatch(expectedArgumentsCount: Int): SemanticErrorMessageBuilder {
        functionArgumentCountMismatch()
        body += "\nThere shall be $expectedArgumentsCount arguments"
        return this
    }

    fun functionArgumentCountMismatch(): SemanticErrorMessageBuilder {
        body += "The number of provided arguments is incorrect"
        return this
    }

    fun functionReturnStatTypeMismatch(functionType: WAny, returnStatType: WAny): SemanticErrorMessageBuilder {
        body += "The \"return\" statement of the function returns $returnStatType, " +
                "but the function has type $functionType"
        return this
    }

    fun functionCallTypeMismatch(assignLhsType: WAny, functionType: WAny): SemanticErrorMessageBuilder {
        body += "Cannot assign function of type $functionType to the variable of type $assignLhsType"
        return this
    }

    fun assignmentTypeMismatch(assignLhsType: WAny, assignRhsType: WAny): SemanticErrorMessageBuilder {
        body += "Cannot assign expression type $assignRhsType to the variable of type $assignLhsType"
        return this
    }

    fun returnFromGlobalScope(): SemanticErrorMessageBuilder {
        body += "Cannot return from global scope!"
        return this
    }

    fun arrayEntriesTypeClash(): SemanticErrorMessageBuilder {
        body += "The elements of the array have inconsistent types!"
        return this
    }

    fun arrayEntriesTypeMismatch(requiredType: WAny, actualType: WAny): SemanticErrorMessageBuilder {
        body += "The elements of the array of type $requiredType[] have incorrect type $actualType"
        return this
    }

    fun nonIntExpressionExit(actualType: WAny): SemanticErrorMessageBuilder {
        body += "Cannot exit with non-int expression. Actual: $actualType"
        return this
    }

    fun nonArrayTypeElemAccess(nonArrayType: WAny): SemanticErrorMessageBuilder {
        body += "Cannot access index elements of non-array type: $nonArrayType"
        return this
    }

    fun operandTypeMismatch(expectedType: WAny, actualType: WAny,
                            expressionText: String = ""): SemanticErrorMessageBuilder {
        val expression = if (expressionText.isNotEmpty()) "\"$expressionText\"" else ""
        body += "The $actualType expression $expression does not conform to the expected type $expectedType"
        return this
    }
}

class SyntaxErrorMessageBuilder : ErrorMessageBuilder() {
    override val prefix = "SYNTAX ERROR"
}

class SemanticException(private val reason: String) : Exception() {
    override val message: String
        get() = "Semantic error!\n$reason"
}