package utils

import waccType.WAny

class SemanticErrorMessageBuilder : ErrorMessageBuilder() {

    override val prefix = "SEMANTIC ERROR"

    override fun provideStart(lineNumber: Int, columnNumber: Int, lineText: String): SemanticErrorMessageBuilder {
        return super.provideStart(lineNumber, columnNumber, lineText) as SemanticErrorMessageBuilder
    }

    override fun provideStart(start: PositionedError): SemanticErrorMessageBuilder {
        return super.provideStart(start) as SemanticErrorMessageBuilder
    }

    override fun appendCustomErrorMessage(msg: String): SemanticErrorMessageBuilder {
        return super.appendCustomErrorMessage(msg) as SemanticErrorMessageBuilder
    }

    fun variableRedeclaration(variableName: String): SemanticErrorMessageBuilder {
        body += "The variable $variableName is already declared"
        return this
    }

    fun variableRedeclaration(): SemanticErrorMessageBuilder {
        body += "The variable is already declared"
        return this
    }

    fun nullPointerFstDereference(): SemanticErrorMessageBuilder {
        body += "The 'snd' operator cannot be applied to a null value"
        return this
    }

    fun nullPointerSndDereference(): SemanticErrorMessageBuilder {
        body += "The 'snd' operator cannot be applied to a null value"
        return this
    }

    fun freeNonPair(): SemanticErrorMessageBuilder {
        body += "Cannot free a value of non-pair type."
        return this
    }

    fun freeNonPair(actualType: WAny): SemanticErrorMessageBuilder {
        freeNonPair()
        body += " The actual type is $actualType"
        return this
    }

    fun readTypeIsIncorrect(actualType: WAny): SemanticErrorMessageBuilder {
        body += "Cannot read into the variable of type $actualType. Variables must be characters or numeric."
        return this
    }

    fun whileStatConditionHasNonBooleanType(actualType: WAny): SemanticErrorMessageBuilder {
        whileStatConditionHasNonBooleanType()
        body += " Got $actualType instead"
        return this
    }

    fun whileStatConditionHasNonBooleanType(): SemanticErrorMessageBuilder {
        body += "The conditional statement in the \"while\" statement does not evaluate to the boolean type."
        return this
    }

    fun ifStatConditionHasNonBooleanType(actualType: WAny): SemanticErrorMessageBuilder {
        ifStatConditionHasNonBooleanType()
        body += " Got $actualType instead"
        return this
    }

    fun ifStatConditionHasNonBooleanType(): SemanticErrorMessageBuilder {
        body += "The conditional statement in the \"if\" statement does not evaluate to the boolean type."
        return this
    }

    fun variableNotInScope(identifier: String): SemanticErrorMessageBuilder {
        body += "The variable $identifier is undefined. It is out of the current scope or any parent scope."
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