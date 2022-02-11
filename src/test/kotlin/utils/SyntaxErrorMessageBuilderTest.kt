package utils

import org.junit.Test
import java.lang.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.fail

class SyntaxErrorMessageBuilderTest {

    private val customMsg = "Testing Syntax Error Message Builder"
    private val lineNum = 5
    private val columnNum = 3
    private val lineText = "int foo = 123 + 34 + 21"
    private val positionedError = PositionedError(lineNum, columnNum, lineText)

    @Test
    fun builderPatternIsIdenticalToUsingConstructors() {

        val buildErrorMessage
            = SyntaxErrorMessageBuilder()
            .appendCustomErrorMessage(customMsg)
            .provideStart(positionedError)
            .build()

        val constructorErrorMessage
            = ErrorMessage("SYNTAX ERROR", PositionedError(lineNum, columnNum, lineText), customMsg)

        assertEquals(buildErrorMessage, constructorErrorMessage)
    }

    @Test
    fun allowsMultipleCustomErrorMessages() {
        try {
            SyntaxErrorMessageBuilder()
                .provideStart(positionedError)
                .appendCustomErrorMessage("Message 1")
                .appendCustomErrorMessage("Message 2")
                .build()
        } catch (e: Exception) {
            e.printStackTrace()
            fail("Appending multiple error messages must be allowed")
        }
    }

    @Test
    fun doNotAllowMultipleLineTextSettings() {
        try {
            SyntaxErrorMessageBuilder()
                .provideStart(positionedError)
                .appendCustomErrorMessage("some message")
                .setLineText("Very original test string!")
                .setLineText("Yet another very original test string!!!")
                .build()
            fail("Multiple calls of line text setter should not be allowed")
        } catch (e: IllegalStateException) {
            assertEquals(ErrorMessageBuilder.LINE_TEXT_ALREADY_SPECIFIED, e.message)
        }
    }
}