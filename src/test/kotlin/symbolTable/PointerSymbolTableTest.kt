package symbolTable

import org.junit.Test
import utils.SemanticException
import waccType.WInt
import waccType.WStr
import kotlin.test.fail

class PointerSymbolTableTest {
    @Test
    fun canDeclareInts() {
        val pointerSymbolTable = PointerSymbolTable()
        pointerSymbolTable.declare("x", WInt())
    }

    @Test
    fun canDeclareStrings() {
        val pointerSymbolTable = PointerSymbolTable()
        pointerSymbolTable.declare("s", WStr())
    }


    @Test
    fun canGetInts() {
        val pointerSymbolTable = PointerSymbolTable()
        pointerSymbolTable.declare("x", WInt())
        pointerSymbolTable.get("x")

    }

    @Test
    fun canGetStrings() {
        val pointerSymbolTable = PointerSymbolTable()
        pointerSymbolTable.declare("x", WStr())
        pointerSymbolTable.get("x")
    }

    @Test
    fun canReassignInt() {
        val pointerSymbolTable = PointerSymbolTable()
        pointerSymbolTable.declare("x", WInt())
        pointerSymbolTable.reassign("x", WInt())

    }

    @Test
    fun canReassignString() {
        val pointerSymbolTable = PointerSymbolTable()
        pointerSymbolTable.declare("x", WStr())
        pointerSymbolTable.reassign("x", WStr())
    }

    @Test
    fun reassigningIntWithStringThrowsSemanticException() {
        val pointerSymbolTable = PointerSymbolTable()
        pointerSymbolTable.declare("x", WInt())
        try {
            pointerSymbolTable.reassign("x", WStr())
            fail() // Should not get here
        } catch (e: SemanticException) {
            println(e)
        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun gettingIntAsStringThrowsSemanticException() {
        val pointerSymbolTable = PointerSymbolTable()
        pointerSymbolTable.declare("x", WInt())
        try {
            pointerSymbolTable.getAndCast<WStr>("x")
            fail() // Should not get here
        } catch (e: SemanticException) {
            println(e)
        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun gettingUndefinedVariableThrowsSemanticException() {
        val pointerSymbolTable = PointerSymbolTable()
        try {
            pointerSymbolTable.get("x")
            fail() // Should not get here
        } catch (e: SemanticException) {
            println(e)
        } catch (e: Exception) {
            fail()
        }
    }

//    @Test
//    fun reassigningUndeclaredVariableThrowsSemanticException() {
//        val pointerSymbolTable = PointerSymbolTable()
//        try {
//            pointerSymbolTable.reassign("x", WInt(1337))
//            fail() // Should not get here
//        } catch (e: SemanticException) {
//            println(e)
//        } catch (e: Exception) {
//            fail()
//        }
//    }

    @Test
    fun canCreateChildScope() {
        val st = PointerSymbolTable()
        st.createChildScope()
    }

    @Test
    fun declaringVariableInChildScopeDoesNotCreateItInParent() {
        val st = PointerSymbolTable()
        val child = st.createChildScope()
        child.declare("x", WInt())
        try {
            st.get("x")
            fail() // Should not get here
        } catch (e: SemanticException) {
            println(e)
        } catch (e: Exception) {
            fail()
        }
    }

//    @Test
//    fun childCanGetVariablesFromParent() {
//        val st = PointerSymbolTable()
//        st.declare("x", WInt(5))
//        val child = st.createChildScope()
//        assertEquals(child.get("x"), WInt(5))
//    }

//    @Test
//    fun childCanReassignDeclaredVariablesInParent() {
//        val st = PointerSymbolTable()
//        st.declare("x", WInt())
//        val child = st.createChildScope()
//        st.reassign("x", WInt(15))
//        assertEquals(st.get("x"), WInt(15))
//        assertEquals(child.get("x"), WInt(15))
//    }

//    @Test
//    fun childCanRedefineSameTypeVariable() {
//        val st = PointerSymbolTable()
//        st.declare("x", WInt(12))
//        val child = st.createChildScope()
//        child.declare("x", WInt(20))
//        assertEquals(st.get("x"), WInt(12))
//        assertEquals(child.get("x"), WInt(20))
//    }
//
//    @Test
//    fun childCanDeclareSameNameButDifferentType() {
//        val st = PointerSymbolTable()
//        st.declare("x", WInt(12))
//        val child = st.createChildScope()
//        child.declare("x", WStr("Hello"))
//        assertEquals(st.get("x"), WInt(12))
//        assertEquals(child.get("x"), WStr("Hello"))
//    }
//
//    @Test
//    fun childCannotReassignParentVariablesAfterNewDeclaration() {
//        val st = PointerSymbolTable()
//        st.declare("x", WInt(12))
//        st.declare("y", WInt(20))
//        val child = st.createChildScope()
//        child.declare("x", WInt(12))
//        child.declare("y", WStr("hi"))
//        child.reassign("x", WInt(16))
//        child.reassign("y", WStr("hello"))
//
//        assertEquals(st.get("x"), WInt(12))
//        assertEquals(st.get("y"), WInt(20))
//    }
//
//    @Test
//    fun grandchildCanReassignGrandparent() {
//        val st = PointerSymbolTable()
//        st.declare("x", WInt(12))
//        val child = st.createChildScope()
//        val grand = child.createChildScope()
//        grand.reassign("x", WInt(15))
//        assertEquals(child.get("x"), WInt(15))
//        assertEquals(st.get("x"), WInt(15))
//    }
//
//    @Test
//    fun grandchildReassignsParentBeforeGrandparent() {
//        val st = PointerSymbolTable()
//        st.declare("x", WInt(12))
//        val child = st.createChildScope()
//        child.declare("x", WInt(15))
//        val grand = child.createChildScope()
//        grand.reassign("x", WInt(17))
//        assertEquals(child.get("x"), WInt(17))
//        assertEquals(st.get("x"), WInt(12))
//    }
//
//    @Test
//    fun grandchildCannotReassignGrandparentIfRedeclareInParent() {
//        val st = PointerSymbolTable()
//        st.declare("x", WInt(12))
//        val child = st.createChildScope()
//        child.declare("x", WStr("hi"))
//        val grand = child.createChildScope()
//        try {
//            grand.reassign("x", WInt(12))
//            fail() // Should not get here
//        } catch (e: SemanticException) {
//            println(e)
//        } catch (e: Exception) {
//            fail()
//        }
//    }
//
//    @Test
//    fun grandchildCannotGetGrandparentIfRedeclareInParent() {
//        val st = PointerSymbolTable()
//        st.declare("x", WInt(12))
//        val child = st.createChildScope()
//        child.declare("x", WStr("Hi"))
//        val grand = child.createChildScope()
//        try {
//            grand.getAndCast<WInt>("x")
//            fail() // Should not get here
//        } catch (e: SemanticException) {
//            println(e)
//        } catch (e: Exception) {
//            fail()
//        }
//    }
}