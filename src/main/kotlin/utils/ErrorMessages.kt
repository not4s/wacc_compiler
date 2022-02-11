package utils

import org.antlr.v4.runtime.ParserRuleContext

data class PositionedError(
    val lineNumber: Int,
    private val columnNumber: Int,
    private var lineText: String = "",
) {
    /**
     * Special constructor which allows creating PositionError from ParserRuleContext
     * @param parserCtx is a ParserRuleContext which has start Attribute,
     * which has everything needed for a PositionedError
     */
    constructor(parserCtx: ParserRuleContext)
            : this(parserCtx.start.line, parserCtx.start.charPositionInLine, parserCtx.start.text)

    override fun toString(): String {
        val linePrefix = "$lineNumber | "
        val arrowAlignment = " ".repeat(linePrefix.length + columnNumber)
        val pointingArrow = "$arrowAlignment^"
        return "Error at [${getCoordinates()}]:\n"+
                "$linePrefix$lineText\n" + pointingArrow
    }

    fun getCoordinates() : String {
        return "$lineNumber: $columnNumber"
    }

    fun setLineText(line: String) {
        lineText = line
    }

    fun getLineText(): String {
        return lineText
    }
}

data class ErrorMessage(
    private val prefix: String,
    private val errorCoordinates: PositionedError,
    private val body: String,
) {
    override fun toString(): String {
        val display: String? = if (errorCoordinates.getLineText().isEmpty()) null else "$errorCoordinates"
        display?.let { return "${errorHeader(prefix)}\n$body\n$display" }
        return "${errorHeader(prefix)}\n$body\nat ${errorCoordinates.getCoordinates()}"
    }

    companion object {
        fun errorHeader(prefix: String): String {
            return "-----------------< $prefix! >-----------------"
        }
    }
}

class SemanticException(val reason: String) : Exception() {
    override val message: String
        get() = "Semantic error!\n$reason"
}