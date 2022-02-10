package utils

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SyntaxErrorMessageBuilderTest {

    private val customMsg = "Testing Syntax Error Message Builder"
    private val lineNum = 23
    private val columnNum = 8
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
}