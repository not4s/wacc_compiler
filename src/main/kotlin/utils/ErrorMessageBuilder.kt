package utils

/**
 * The class is abstract in order to avoid creating Errors which have
 * semantic nature but syntax error message body and vice versa
 */
abstract class ErrorMessageBuilder {
    protected abstract val prefix: String
    private var body: String = ""
    private lateinit var start: PositionedError

    private fun prependNewLineIfNeeded() {
        if (body.isNotEmpty() && body.last() != '\n') {
            body += "\n"
        }
    }

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

    /**
     * Wrapper around the public error messages. Before appending the message
     * the new line prepended if the previous line does not end with new line character.
     * @param msg is the string which is appended to a body and newLine if needed
     */
    open fun appendCustomErrorMessage(msg: String): ErrorMessageBuilder {
        prependNewLineIfNeeded()
        body += msg
        return this
    }
}