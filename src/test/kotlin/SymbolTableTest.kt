import org.junit.Test
import utils.SemanticException
import kotlin.test.assertEquals
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

    @Test
    fun canCreateChildScope() {
        val st = SymbolTable()
        val child = st.createChildScope()
    }

    @Test
    fun declaringVariableInChildScopeDoesNotCreateItInParent() {
        val st = SymbolTable()
        val child = st.createChildScope()
        child.declare("x", 5)
        try {
            st.get<Int>("x")
            fail() // Should not get here
        } catch (e: SemanticException) {
            println(e)
        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun childCanGetVariablesFromParent() {
        val st = SymbolTable()
        st.declare("x", 12)
        val child = st.createChildScope()
        assertEquals(child.get("x"), 12)
    }

    @Test
    fun childCanReassignDeclaredVariablesInParent() {
        val st = SymbolTable()
        st.declare("x", 12)
        val child = st.createChildScope()
        st.reassign("x", 15)
        assertEquals(st.get("x"), 15)
        assertEquals(child.get("x"), 15)
    }

    @Test
    fun childCanRedefineSameTypeVariable() {
        val st = SymbolTable()
        st.declare("x", 12)
        val child = st.createChildScope()
        child.declare("x", 20)
        assertEquals(st.get("x"), 12)
        assertEquals(child.get("x"), 20)
    }

    @Test
    fun childCanDeclareSameNameButDifferentType() {
        val st = SymbolTable()
        st.declare("x", 12)
        val child = st.createChildScope()
        child.declare("x", "Hello!")
        assertEquals(st.get("x"), 12)
        assertEquals(child.get("x"), "Hello!")
    }

    @Test
    fun childCannotReassignParentVariablesAfterNewDeclaration() {
        val st = SymbolTable()
        st.declare("x", 12)
        st.declare("y", 20)
        val child = st.createChildScope()
        child.declare("x", 13)
        child.declare("y", "Hi")
        child.reassign("x", 16)
        child.reassign("y", "Yo")

        assertEquals(st.get("x"), 12)
        assertEquals(st.get("y"), 20)
    }

    @Test
    fun grandchildCanReassignGrandparent() {
        val st = SymbolTable()
        st.declare("x", 12)
        val child = st.createChildScope()
        val grand = child.createChildScope()
        grand.reassign("x", 15)
        assertEquals(child.get("x"), 15)
        assertEquals(st.get("x"), 15)
    }

    @Test
    fun grandchildReassignsParentBeforeGrandparent() {
        val st = SymbolTable()
        st.declare("x", 12)
        val child = st.createChildScope()
        child.declare("x", 15)
        val grand = child.createChildScope()
        grand.reassign("x", 17)
        assertEquals(child.get("x"), 17)
        assertEquals(st.get("x"), 12)
    }

    @Test
    fun grandchildCannotReassignGrandparentIfRedeclareInParent() {
        val st = SymbolTable()
        st.declare("x", 12)
        val child = st.createChildScope()
        child.declare("x", "Hi")
        val grand = child.createChildScope()
        try {
            grand.reassign("x", 15)
            fail() // Should not get here
        } catch (e: SemanticException) {
            println(e)
        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun grandchildCannotGetGrandparentIfRedeclareInParent() {
        val st = SymbolTable()
        st.declare("x", 12)
        val child = st.createChildScope()
        child.declare("x", "Hi")
        val grand = child.createChildScope()
        try {
            grand.get<Int>("x")
            fail() // Should not get here
        } catch (e: SemanticException) {
            println(e)
        } catch (e: Exception) {
            fail()
        }
    }
}