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
    fun canDeclareInts() {
        val symbolTable = SymbolTable()
        symbolTable.declare("x", 1337)
    }

    @Test
    fun canDeclareStrings() {
        val symbolTable = SymbolTable()
        symbolTable.declare("s", "Hello world!")
    }

    @Test
    fun canGetInts() {
        val symbolTable = SymbolTable()
        symbolTable.declare("x", 1337)
        assertEquals(symbolTable.get("x"), 1337)

    }

    @Test
    fun canGetStrings() {
        val symbolTable = SymbolTable()
        symbolTable.declare("s", "Hello world!")
        assertEquals(symbolTable.get("s"), "Hello world!")
    }

    @Test
    fun canReassignInt() {
        val symbolTable = SymbolTable()
        symbolTable.declare("x", 1337)
        symbolTable.reassign("x", 42)
        assertEquals(symbolTable.get("x"), 42)

    }

    @Test
    fun canReassignString() {
        val symbolTable = SymbolTable()
        symbolTable.declare("s", "Hello world!")
        symbolTable.reassign("s", "Goodbye world.")
        assertEquals(symbolTable.get("s"), "Goodbye world.")
    }

    @Test
    fun reassigningIntWithStringThrowsSemanticException() {
        val symbolTable = SymbolTable()
        symbolTable.declare("x", 1337)
        try {
            symbolTable.reassign("x", "This is not an int")
            fail() // Should not get here
        } catch (e: SemanticException) {
            println(e)
        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun gettingIntAsStringThrowsSemanticException() {
        val symbolTable = SymbolTable()
        symbolTable.declare("x", 1337)
        try {
            val s : String = symbolTable.get("x")
            fail() // Should not get here
        } catch (e: SemanticException) {
            println(e)
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
        } catch (e: SemanticException) {
            println(e)
        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun reassigningUndeclaredVariableThrowsSemanticException() {
        val symbolTable = SymbolTable()
        try {
            symbolTable.reassign("x", 1337)
            fail() // Should not get here
        } catch (e: SemanticException) {
            println(e)
        } catch (e: Exception) {
            fail()
        }
    }
}