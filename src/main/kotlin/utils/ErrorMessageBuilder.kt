package utils

import java.io.File

/**
 * The class is abstract in order to avoid creating Errors which have
 * semantic nature but syntax error message body and vice versa
 */
abstract class ErrorMessageBuilder {

    companion object {
        const val UNINITIALIZED_LINE_TEXT: String = "The lineText is not specified!"
        const val LINE_TEXT_ALREADY_SPECIFIED: String = "The code text of the ErrorMessage has already been specified"
        const val UNINITIALIZED_START: String = "The 'start' property is not initialised. Use provideStart()"
        const val SET_START_ONCE_RESTRICTION: String = "The 'start' property must be set only once."
        const val SPECIFIC_MESSAGE_RESTRICTION : String
            = "Only a single specific error message can be added. Use appendCustomErrorMessage() method instead."
    }

    protected abstract val prefix: String
    private var body: String = ""
    private var start: PositionedError? = null
    private var theSpecificMessageIsAppended: Boolean = false
    private var theLineTextSpecified: Boolean = false

    private fun prependNewLineIfNeeded() {
        if (body.isNotEmpty() && body.last() != '\n') {
            body += "\n"
        }
    }

    fun build(): ErrorMessage {
        val startParam = start ?: throw IllegalStateException(UNINITIALIZED_START)
        if (!theLineTextSpecified) {
            throw IllegalStateException(UNINITIALIZED_LINE_TEXT)
        }
        return ErrorMessage(prefix, startParam, body)
    }

    fun buildAndPrint(): ErrorMessage {
        val errorMessage = build()
        println(errorMessage.toString())
        return errorMessage
    }

    /**
     * The following function or its other overloaded version must be
     * called once during building process
     */
    open fun provideStart(lineNumber: Int, columnNumber: Int): ErrorMessageBuilder {
        return provideStart(PositionedError(lineNumber, columnNumber, ""))
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

    /**
     * Sets the string value of the code text of a line which contains error
     * a call to this function is required to get the complete message
     * @throws IllegalStateException if it was called before initializing start
     */
    open fun setLineText(codeText: String): ErrorMessageBuilder {
        if (theLineTextSpecified) {
            throw IllegalStateException(LINE_TEXT_ALREADY_SPECIFIED)
        }
        val safeStart = start ?: throw IllegalStateException(UNINITIALIZED_START)
        safeStart.setLineText(codeText)
        theLineTextSpecified = true
        return this
    }

    /**
     * Alternative to setLineText(), which sets the line text from a file
     * a call to this function is required to get the complete message
     * @throws IllegalStateException if it was called before initializing start
     */
    open fun setLineTextFromSrcFile(srcFilePath: String): ErrorMessageBuilder {
        val safeStart = start ?: throw IllegalStateException(UNINITIALIZED_START)
        val lines = File(srcFilePath).readLines()
        // sanity check against out of bounds errors
        return if (safeStart.lineNumber <= lines.size) {
            setLineText(lines[safeStart.lineNumber - 1])
        } else {
            setLineText(lines.last())
        }
    }
}