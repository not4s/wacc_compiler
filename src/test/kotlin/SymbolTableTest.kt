import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

class SymbolTableTest {
    @Test
    fun emptyWhenInitialized() {
        val symbolTable = SymbolTable()
        assertTrue(symbolTable.table.isEmpty())
    }

    @Test
    fun canAddInts() {
        val symbolTable = SymbolTable()
        symbolTable.put("x", 1337)
    }

    @Test
    fun canAddStrings() {
        val symbolTable = SymbolTable()
        symbolTable.put("s", "Hello world!")
    }

    @Test
    fun canGetInts() {
        val symbolTable = SymbolTable()
        symbolTable.put("x", 1337)
        assertEquals(symbolTable.get("x"), 1337)

    }

    @Test
    fun canGetStrings() {
        val symbolTable = SymbolTable()
        symbolTable.put("s", "Hello world!")
        assertEquals(symbolTable.get("s"), "Hello world!")
    }

    @Test
    fun canOverwriteInt() {
        val symbolTable = SymbolTable()
        symbolTable.put("x", 1337)
        symbolTable.put("x", 42)
        assertEquals(symbolTable.get("x"), 42)

    }

    @Test
    fun canOverwriteString() {
        val symbolTable = SymbolTable()
        symbolTable.put("s", "Hello world!")
        symbolTable.put("s", "Goodbye world.")
        assertEquals(symbolTable.get("s"), "Goodbye world.")
    }

    @Test
    fun overwritingIntWithStringThrowsSemanticException() {
        val symbolTable = SymbolTable()
        symbolTable.put("x", 1337)
        try {
            symbolTable.put("x", "This is not an int")
            fail() // Should not get here
        } catch (_: SemanticException) {

        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun gettingIntAsStringThrowsSemanticException() {
        val symbolTable = SymbolTable()
        symbolTable.put("x", 1337)
        try {
            val s : String = symbolTable.get("x")
            fail() // Should not get here
        } catch (_: SemanticException) {

        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun gettingUndefinedVariableThrowsSemanticException() {
        val symbolTable = SymbolTable()
        try {
            val x : String = symbolTable.get("x")
            fail() // Should not get here
        } catch (_: SemanticException) {

        } catch (e: Exception) {
            fail()
        }
    }
}