package utils

import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class SemanticErrorMessageBuilderTest {

    @Test
    fun doesNotAllowMultipleTypeErrorMessages() {
        try {
            SemanticErrorMessageBuilder()
                .functionRedefineError()
                .functionArgumentCountMismatch()
                .freeNonPair()
            fail("Should not allow multiple errors here")
        } catch (e: IllegalStateException) {
            assertTrue { e.message?.contains("multiple errors") ?: false }
        }
    }
}