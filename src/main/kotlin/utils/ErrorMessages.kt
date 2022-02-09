package utils

import antlr.WACCParser
import org.antlr.v4.runtime.ParserRuleContext

data class PositionedError(
    private val lineNumber: Int,
    private val columnNumber: Int,
    private val lineText: String,
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
        val arrowLength = 3
        val arrowAlignment = " ".repeat(linePrefix.length + columnNumber)
        val pointingArrow = "$arrowAlignment|\n".repeat(arrowLength - 1) + arrowAlignment + "V\n"
        return "Error at line $lineNumber, position $columnNumber as follows:\n"+
                pointingArrow + "$linePrefix$lineText\n"
    }
}

data class ErrorMessage(
    private val prefix: String,
    private val start: PositionedError,
    private val body: String,
) {
    override fun toString(): String {
        return errorHeader(prefix) + "\n\n" +
                start + "\n\n" +
                body + "\n\n"
    }

    companion object {
        fun errorHeader(prefix: String): String {
            return "----------< $prefix! >----------"
        }
    }
}

class SemanticException(private val reason: String) : Exception() {
    override val message: String
        get() = "Semantic error!\n$reason"
}