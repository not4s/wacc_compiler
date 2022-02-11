package syntax

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import utils.ExitCode
import utils.SyntaxErrorMessageBuilder
import java.io.File
import kotlin.system.exitProcess

class SyntaxErrBuilderErrorListener(val sourceFile: File) : BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        SyntaxErrorMessageBuilder()
            .provideStart(line - 1, charPositionInLine)
            .setLineTextFromSrcFile(sourceFile.absolutePath)
            .appendCustomErrorMessage(msg ?: "Null message")
            .buildAndPrint()
        exitProcess(ExitCode.SYNTAX_ERROR)
    }
}
