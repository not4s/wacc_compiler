package symbolTable

import org.junit.Test
import utils.SemanticException
import waccType.*
import kotlin.test.fail

class ParentRefSymbolTableTest {

    private val parentRefSymbolTable
        = ParentRefSymbolTable("/path/to/some/file/foo/bar/fizz/buzz.wacc")

    @Test
    fun canDeclareInts() {
        parentRefSymbolTable.declare("x", WInt())
    }

    @Test
    fun canDeclareStrings() {
        parentRefSymbolTable.declare("s", WStr())
    }


    @Test
    fun canGetInts() {
        parentRefSymbolTable.declare("x", WInt())
        parentRefSymbolTable.get("x")

    }

    @Test
    fun canGetStrings() {
        parentRefSymbolTable.declare("x", WStr())
        parentRefSymbolTable.get("x")
    }

    @Test
    fun canReassignInt() {
        parentRefSymbolTable.declare("x", WInt())
        parentRefSymbolTable.reassign("x", WInt())

    }

    @Test
    fun canReassignString() {
        parentRefSymbolTable.declare("x", WStr())
        parentRefSymbolTable.reassign("x", WStr())
    }

    @Test
    fun reassigningIntWithStringThrowsSemanticException() {
        parentRefSymbolTable.declare("x", WInt())
        try {
            parentRefSymbolTable.reassign("x", WStr())
            fail() // Should not get here
        } catch (e: SemanticException) {
            println(e)
        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun gettingIntAsStringThrowsSemanticException() {
        parentRefSymbolTable.declare("x", WInt())
        try {
            parentRefSymbolTable.getAndCast<WStr>("x")
            fail() // Should not get here
        } catch (e: SemanticException) {
            println(e)
        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun gettingUndefinedVariableThrowsSemanticException() {
        try {
            parentRefSymbolTable.get("x")
            fail() // Should not get here
        } catch (e: SemanticException) {
            println(e)
        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun canCreateChildScope() {
        parentRefSymbolTable.createChildScope()
    }

    @Test
    fun declaringVariableInChildScopeDoesNotCreateItInParent() {
        val st = parentRefSymbolTable
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

    @Test
    fun canDeclareArrays() {
        parentRefSymbolTable.declare("array", WArray(WUnknown()))
    }

    @Test
    fun canDeclareArrayOfBaseTypes() {
        parentRefSymbolTable.declare("array_1", WArray(WInt()))
        parentRefSymbolTable.declare("array_2", WArray(WStr()))
        parentRefSymbolTable.declare("array_3", WArray(WBool()))
        parentRefSymbolTable.declare("array_4", WArray(WChar()))
    }

    @Test
    fun canDeclareArrayOfPairs() {
        val pairUnknownUnknown = WPair(WUnknown(), WUnknown())
        val pairStringInt = WPair(WStr(), WInt())
        val pairStringPairStringInt = WPair(WStr(), pairStringInt)
        parentRefSymbolTable.declare("array_1", pairUnknownUnknown)
        parentRefSymbolTable.declare("array_2", pairStringInt)
        parentRefSymbolTable.declare("array_3", pairStringPairStringInt)
    }

    @Test
    fun canDeclareArrayOfArrays() {
        val aI = WArray(WInt())
        val aaI = WArray(aI)
        val aaaI = WArray(aaI)
        parentRefSymbolTable.declare("array_1", aI)
        parentRefSymbolTable.declare("array_2", aaI)
        parentRefSymbolTable.declare("array_3", aaaI)
    }

    @Test
    fun canAssignValuesInBaseTypeArray() {
        val arrayName = "array"
        // invalid array indexing is a run-time error
        val indices: Array<WInt> = arrayOf(WInt())
        parentRefSymbolTable.declare(arrayName, WArray(WInt()))
        parentRefSymbolTable.reassign(arrayName, indices, WInt())
    }

    @Test
    fun canAssignValuesInPairArrays() {
        val arrayName = "array"
        val pairStringPairUnknownUnknown = WPair(WStr(), WPair(WUnknown(), WUnknown()))
        // invalid array indexing is a run-time error
        val indices: Array<WInt> = arrayOf(WInt())
        parentRefSymbolTable.declare(arrayName, WArray(pairStringPairUnknownUnknown))
        parentRefSymbolTable.reassign(arrayName, indices, pairStringPairUnknownUnknown)
        try { parentRefSymbolTable.reassign(arrayName, indices, WInt()) }
        catch (e: SemanticException) { println(e) }
    }
}