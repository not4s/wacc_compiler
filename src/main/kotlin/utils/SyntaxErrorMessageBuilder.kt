package utils

class SyntaxErrorMessageBuilder : ErrorMessageBuilder() {

    override val prefix = "SYNTAX ERROR"

    override fun provideStart(lineNumber: Int, columnNumber: Int, lineText: String): SyntaxErrorMessageBuilder {
        return super.provideStart(lineNumber, columnNumber, lineText) as SyntaxErrorMessageBuilder
    }

    override fun provideStart(start: PositionedError): SyntaxErrorMessageBuilder {
        return super.provideStart(start) as SyntaxErrorMessageBuilder
    }

    override fun appendCustomErrorMessage(msg: String): SyntaxErrorMessageBuilder {
        return super.appendCustomErrorMessage(msg) as SyntaxErrorMessageBuilder
    }

    fun generalSyntaxErrorMessage(): SyntaxErrorMessageBuilder {
        body += "Cannot parse the given code"
        return this
    }
}