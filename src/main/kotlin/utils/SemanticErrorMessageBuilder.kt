package utils

import waccType.WAny

class SemanticErrorMessageBuilder : ErrorMessageBuilder() {

    override val prefix = "SEMANTIC ERROR"

    override fun provideStart(lineNumber: Int, columnNumber: Int, lineText: String): SemanticErrorMessageBuilder {
        return super.provideStart(lineNumber, columnNumber, lineText) as SemanticErrorMessageBuilder
    }

    override fun provideStart(startProvided: PositionedError): SemanticErrorMessageBuilder {
        return super.provideStart(startProvided) as SemanticErrorMessageBuilder
    }

    override fun appendCustomErrorMessage(msg: String): SemanticErrorMessageBuilder {
        return super.appendCustomErrorMessage(msg) as SemanticErrorMessageBuilder
    }

    override fun appendSpecificErrorMessage(msg: String): SemanticErrorMessageBuilder {
        return super.appendSpecificErrorMessage(msg) as SemanticErrorMessageBuilder
    }

    fun variableRedeclaration(variableName: String): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("The variable $variableName is already declared")
    }

    fun variableRedeclaration(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("The variable is already declared")
    }

    fun nullPointerFstDereference(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("The 'snd' operator cannot be applied to a null value")
    }

    fun nullPointerSndDereference(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("The 'snd' operator cannot be applied to a null value")
    }

    fun freeNonPair(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("Cannot free a value of non-pair type.")
    }

    fun freeNonPair(actualType: WAny): SemanticErrorMessageBuilder {
        freeNonPair()
        return appendSpecificErrorMessage("The actual type is $actualType")
    }

    fun readTypeIsIncorrect(actualType: WAny): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "Cannot read into the variable of type $actualType. Variables must be characters or numeric.")
    }

    fun whileStatConditionHasNonBooleanType(actualType: WAny): SemanticErrorMessageBuilder {
        whileStatConditionHasNonBooleanType()
        return appendSpecificErrorMessage("Got $actualType instead")
    }

    fun whileStatConditionHasNonBooleanType(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "The conditional statement in the \"while\" statement does not evaluate to the boolean type.")
    }

    fun ifStatConditionHasNonBooleanType(actualType: WAny): SemanticErrorMessageBuilder {
        ifStatConditionHasNonBooleanType()
        return appendSpecificErrorMessage(" Got $actualType instead")
    }

    fun ifStatConditionHasNonBooleanType(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "The conditional statement in the \"if\" statement does not evaluate to the boolean type.")
    }

    fun variableNotInScope(identifier: String): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "The variable $identifier is undefined. It is out of the current scope or any parent scope.")
    }

    fun functionArgumentTypeMismatch(expectedType: WAny, actualType: WAny): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "The supplied function argument has incorrect type. Expected $expectedType, got $actualType instead.")
    }

    fun functionRedefineError(functionName: String): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("Cannot redefine function $functionName")
    }

    fun functionRedefineError(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("Cannot redefine function")
    }

    fun functionArgumentCountMismatch(expectedArgumentsCount: Int): SemanticErrorMessageBuilder {
        functionArgumentCountMismatch()
        return appendSpecificErrorMessage("There shall be $expectedArgumentsCount arguments")
    }

    fun functionArgumentCountMismatch(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("The number of provided arguments is incorrect")
    }

    fun functionReturnStatTypeMismatch(functionType: WAny, returnStatType: WAny): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("The \"return\" statement of the function returns $returnStatType, " +
                "but the function has type $functionType")
    }

    fun functionCallTypeMismatch(assignLhsType: WAny, functionType: WAny): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "Cannot assign function of type $functionType to the variable of type $assignLhsType")
    }

    fun assignmentTypeMismatch(assignLhsType: WAny, assignRhsType: WAny): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "Cannot assign expression type $assignRhsType to the variable of type $assignLhsType")
    }

    fun returnFromGlobalScope(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("Cannot return from global scope!")
    }

    fun arrayEntriesTypeClash(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("The elements of the array have inconsistent types!")
    }

    fun arrayEntriesTypeMismatch(requiredType: WAny, actualType: WAny): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("The elements of the array of type $requiredType[] have incorrect type $actualType")
    }

    fun nonIntExpressionExit(actualType: WAny): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("Cannot exit with non-int expression. Actual: $actualType")
    }

    fun nonArrayTypeElemAccess(nonArrayType: WAny): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("Cannot access index elements of non-array type: $nonArrayType")
    }

    fun operandTypeMismatch(expectedType: WAny, actualType: WAny): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "The $actualType expression does not conform to the expected type $expectedType")
    }
}