package utils

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

    fun buildAndDisplay(): ErrorMessage {
        val errorMessage = build()
        println(errorMessage.toString())
        return errorMessage
    }

    open fun provideStart(lineNumber: Int, columnNumber: Int, lineText: String): ErrorMessageBuilder {
        this.start = PositionedError(lineNumber, columnNumber, lineText)
        return this
    }

    open fun provideStart(start: PositionedError): ErrorMessageBuilder {
        this.start = start
        return this
    }

    open fun appendCustomErrorMessage(msg: String): ErrorMessageBuilder {
        body += msg
        return this
    }
}