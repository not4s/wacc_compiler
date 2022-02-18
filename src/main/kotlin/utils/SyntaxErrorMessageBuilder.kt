package utils

class SyntaxErrorMessageBuilder : ErrorMessageBuilder() {

    override val prefix = "SYNTAX ERROR"

    override fun provideStart(lineNumber: Int, columnNumber: Int): SyntaxErrorMessageBuilder {
        return super.provideStart(lineNumber, columnNumber) as SyntaxErrorMessageBuilder
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

    override fun setLineTextFromSrcFile(srcFilePath: String): SyntaxErrorMessageBuilder {
        return super.setLineTextFromSrcFile(srcFilePath) as SyntaxErrorMessageBuilder
    }

}