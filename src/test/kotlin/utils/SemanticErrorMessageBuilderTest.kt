package utils

import org.junit.Test
import utils.ErrorMessageBuilder.Companion.UNINITIALIZED_START
import waccType.WAny
import waccType.WInt
import kotlin.test.assertEquals
import kotlin.test.fail

class SemanticErrorMessageBuilderTest {

    private val customMsg = "Testing Semantic Error Message Builder"
    private val lineNum = 234
    private val columnNum = 5
    private val lineText = "bool bar = true || false || true && false"
    private val positionedError = PositionedError(lineNum, columnNum, lineText)

    @Test
    fun doesNotAllowMultipleTypeErrorMessages() {
        try {
            SemanticErrorMessageBuilder()
                .provideStart(positionedError)
                .functionRedefineError()
                .functionArgumentCountMismatch()
                .freeNonPair()
                .build()
            fail("Should not allow multiple errors here")
        } catch (e: IllegalStateException) {
            assertEquals(ErrorMessageBuilder.SPECIFIC_MESSAGE_RESTRICTION, e.message)
        }
    }

    @Test
    fun allowsAddingCustomErrorMessagesBeforeAndAfterSpecificErrorMessage() {
        try {
            SemanticErrorMessageBuilder()
                .provideStart(positionedError)
                .setLineText(lineText)
                .appendCustomErrorMessage("prefix")
                .functionArgumentCountMismatch()
                .appendCustomErrorMessage("suffix")
                .build()
        } catch (e: Exception) {
            e.printStackTrace()
            fail("Adding many custom error messages before or after specific error message must be allowed")
        }
    }

    @Test
    fun doesNotAllowSettingStartMultipleTimes() {
        try {
            SemanticErrorMessageBuilder()
                .provideStart(positionedError)
                .provideStart(lineNum, columnNum, customMsg)
                .appendCustomErrorMessage("some message")
                .build()
            fail("Start should be set once")
        } catch (e: IllegalStateException) {
            assertEquals(ErrorMessageBuilder.SET_START_ONCE_RESTRICTION, e.message)
        }
    }

    @Test
    fun requiresInitialisingStart() {
        try {
            SemanticErrorMessageBuilder()
                .ifStatConditionHasNonBooleanType()
                .appendCustomErrorMessage("Some message")
                .appendCustomErrorMessage("foo")
                .build()
            fail("Somehow managed to build ErrorMessage without start")
        } catch (e: IllegalStateException) {
            assertEquals(UNINITIALIZED_START, e.message)
        }
    }

    @Test
    fun usingDifferentOverloadedFunctionsDoesNotResultIntoConflict() {
        try {
            // Calling specific error message without arguments
            SemanticErrorMessageBuilder()
                .provideStart(positionedError)
                .setLineText(lineText)
                .freeNonPair(WInt())
                .build()
            SemanticErrorMessageBuilder()
                .provideStart(positionedError)
                .setLineText(lineText)
                .whileStatConditionHasNonBooleanType()
                .build()
            SemanticErrorMessageBuilder()
                .provideStart(positionedError)
                .setLineText(lineText)
                .ifStatConditionHasNonBooleanType()
                .build()
            SemanticErrorMessageBuilder()
                .provideStart(positionedError)
                .setLineText(lineText)
                .functionArgumentCountMismatch()
                .build()

            // Calling specific error message with arguments
            SemanticErrorMessageBuilder()
                .provideStart(positionedError)
                .setLineText(lineText)
                .freeNonPair(WInt())
                .build()
            SemanticErrorMessageBuilder()
                .provideStart(positionedError)
                .setLineText(lineText)
                .whileStatConditionHasNonBooleanType(WInt())
                .build()
            SemanticErrorMessageBuilder()
                .provideStart(positionedError)
                .setLineText(lineText)
                .ifStatConditionHasNonBooleanType(WInt())
                .build()
            SemanticErrorMessageBuilder()
                .provideStart(positionedError)
                .setLineText(lineText)
                .functionArgumentCountMismatch(5)
                .build()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            fail("Should not result into an error")
        }
    }

    @Test
    fun doesNotAllowToSetLineTextWhenStartIsNotProvided() {
        try {
            SemanticErrorMessageBuilder()
                .setLineText(lineText)
                .provideStart(positionedError)
                .whileStatConditionHasNonBooleanType(WInt())
                .build()
            fail("Cannot set lineText when start is not initialized")
        } catch (e: IllegalStateException) {
            assertEquals(UNINITIALIZED_START, e.message)
        }
    }
}