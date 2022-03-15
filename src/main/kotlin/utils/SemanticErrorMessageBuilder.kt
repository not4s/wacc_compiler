package utils

import waccType.WAny

class SemanticErrorMessageBuilder : ErrorMessageBuilder() {

    override val prefix = "SEMANTIC ERROR"

    override fun provideStart(lineNumber: Int, columnNumber: Int): SemanticErrorMessageBuilder {
        return super.provideStart(lineNumber, columnNumber) as SemanticErrorMessageBuilder
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

    override fun setLineText(codeText: String): SemanticErrorMessageBuilder {
        return super.setLineText(codeText) as SemanticErrorMessageBuilder
    }

    override fun setLineTextFromSrcFile(srcFilePath: String): SemanticErrorMessageBuilder {
        return super.setLineTextFromSrcFile(srcFilePath) as SemanticErrorMessageBuilder
    }

    fun variableRedeclaration(variableName: String): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("The variable $variableName is already declared")
    }

    fun freeNonPair(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("Cannot free a value of non-pair type.")
    }

    fun freeNonPair(actualType: WAny): SemanticErrorMessageBuilder {
        freeNonPair()
        return appendCustomErrorMessage("The actual type is $actualType")
    }

    fun readTypeIsIncorrect(actualType: WAny): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "Cannot read into the variable of type $actualType. Variables must be characters or numeric."
        )
    }

    fun whileStatConditionHasNonBooleanType(actualType: WAny): SemanticErrorMessageBuilder {
        whileStatConditionHasNonBooleanType()
        return appendCustomErrorMessage("Got $actualType instead")
    }

    fun whileStatConditionHasNonBooleanType(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "The conditional statement in the \"while\" statement does not evaluate to the boolean type."
        )
    }

    fun ifStatConditionHasNonBooleanType(actualType: WAny): SemanticErrorMessageBuilder {
        ifStatConditionHasNonBooleanType()
        return appendCustomErrorMessage(" Got $actualType instead")
    }

    fun ifStatConditionHasNonBooleanType(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "The conditional statement in the \"if\" statement does not evaluate to the boolean type."
        )
    }

    fun variableNotInScope(identifier: String): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "The variable $identifier is undefined. It is out of the current scope or any parent scope."
        )
    }

    fun functionArgumentTypeMismatch(
        expectedType: WAny,
        actualType: WAny
    ): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "The supplied function argument has incorrect type. Expected $expectedType, got $actualType instead."
        )
    }

    fun functionRedefineError(functionName: String): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("Cannot redefine function $functionName")
    }

    fun functionRedefineError(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("Cannot redefine function")
    }

    fun structRedefineError(structName: String): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("Cannot redefine struct $structName")
    }

    fun functionArgumentCountMismatch(
        expectedNum: Int,
        actualNum: Int
    ): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("The number of provided arguments ($actualNum) is incorrect, expected : $expectedNum")
    }

    fun functionReturnStatTypeMismatch(
        functionType: WAny,
        returnStatType: WAny
    ): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "The \"return\" statement of the function returns $returnStatType, " +
                    "but the function has type $functionType"
        )
    }

    fun assignmentTypeMismatch(
        assignLhsType: WAny,
        assignRhsType: WAny
    ): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "Cannot assign expression type $assignRhsType to the variable of type $assignLhsType"
        )
    }

    fun returnFromGlobalScope(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("Cannot return from global scope!")
    }

    fun arrayEntriesTypeClash(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("The elements of the array have inconsistent types!")
    }

    fun nonIntExpressionExit(actualType: WAny): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("Cannot exit with non-int expression. Actual: $actualType")
    }

    fun nonArrayTypeElemAccess(nonArrayType: WAny): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage("Cannot access index elements of non-array type: $nonArrayType")
    }

    fun operandTypeMismatch(expectedType: WAny, actualType: WAny): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "The $actualType expression does not conform to the expected type $expectedType"
        )
    }

    fun binOpInvalidType(actualType: WAny, operation: String): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "Cannot call this binary operation ($operation) on $actualType"
        )
    }

    fun unOpInvalidType(actualType: WAny, operation: String): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "Cannot call this unary operation ($operation) on $actualType"
        )
    }

    fun arrayIndexInvalidType(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "The indexes of the array have an non-int Type"
        )
    }

    fun pairElementInvalidType(): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "The pair element has an invalid type"
        )
    }

    fun structContainsDuplicateElements(repeatedIdentifier : String): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "Repeated identifier `$repeatedIdentifier` found"
        )
    }

    fun elementDoesntExistInStruct(structID: String, nonExistantStructElem : String): SemanticErrorMessageBuilder {
        return appendSpecificErrorMessage(
            "Tried to access non-existent element ${structID}.${nonExistantStructElem}"
        )
    }
}