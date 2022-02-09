package utils

/**
 * The class is abstract in order to avoid creating Errors which have
 * semantic nature but syntax error message body and vice versa
 */
abstract class ErrorMessageBuilder {

    companion object {
        const val UNINITIALIZED_START: String = "The 'start' property is not initialised. Use provideStart()"
        const val SET_START_ONCE_RESTRICTION: String = "The 'start' property must be set only once."
        const val SPECIFIC_MESSAGE_RESTRICTION : String
            = "Only a single specific error message can be added. Use appendCustomErrorMessage() method instead."
    }

    protected abstract val prefix: String
    private var body: String = ""
    private var start: PositionedError? = null
    private var theSpecificMessageIsAppended = false

    private fun prependNewLineIfNeeded() {
        if (body.isNotEmpty() && body.last() != '\n') {
            body += "\n"
        }
    }

    fun build(): ErrorMessage {
        val startParam = start ?: throw IllegalStateException(UNINITIALIZED_START)
        return ErrorMessage(prefix, startParam, body)
    }

    fun buildAndDisplay(): ErrorMessage {
        val errorMessage = build()
        println(errorMessage.toString())
        return errorMessage
    }

    /**
     * The following function or its other overloaded version must be
     * called once during building process
     */
    open fun provideStart(lineNumber: Int, columnNumber: Int, lineText: String = ""): ErrorMessageBuilder {
        return provideStart(PositionedError(lineNumber, columnNumber, lineText))
    }

    open fun provideStart(startProvided: PositionedError): ErrorMessageBuilder {
        if (this.start != null) {
            throw IllegalStateException(SET_START_ONCE_RESTRICTION)
        }
        this.start = startProvided
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

    /**
     * Apart from adding the specific message also restricts only a single
     * call of such method will be done during building process
     */
    open fun appendSpecificErrorMessage(msg: String): ErrorMessageBuilder {
        if (theSpecificMessageIsAppended) {
            throw IllegalStateException(SPECIFIC_MESSAGE_RESTRICTION)
        }
        theSpecificMessageIsAppended = true
        return appendCustomErrorMessage(msg)
    }
}