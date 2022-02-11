package utils

class SyntaxErrorMessageBuilder : ErrorMessageBuilder() {

    override val prefix = "SYNTAX ERROR"

    override fun provideStart(lineNumber: Int, columnNumber: Int, lineText: String): SyntaxErrorMessageBuilder {
        return super.provideStart(lineNumber, columnNumber, lineText) as SyntaxErrorMessageBuilder
    }

    override fun provideStart(startProvided: PositionedError): SyntaxErrorMessageBuilder {
        return super.provideStart(startProvided) as SyntaxErrorMessageBuilder
    }

    override fun appendCustomErrorMessage(msg: String): SyntaxErrorMessageBuilder {
        return super.appendCustomErrorMessage(msg) as SyntaxErrorMessageBuilder
    }

    override fun appendSpecificErrorMessage(msg: String): SyntaxErrorMessageBuilder {
        return super.appendSpecificErrorMessage(msg) as SyntaxErrorMessageBuilder
    }

    override fun setLineText(codeText: String): SyntaxErrorMessageBuilder {
        return super.setLineText(codeText) as SyntaxErrorMessageBuilder
    }

    fun generalSyntaxErrorMessage(): SyntaxErrorMessageBuilder {
        return appendCustomErrorMessage("Cannot parse the given code!")
    }
}