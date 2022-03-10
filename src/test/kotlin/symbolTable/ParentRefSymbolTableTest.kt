package symbolTable

import org.junit.Test
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.*
import kotlin.test.fail

class ParentRefSymbolTableTest {

    private val parentRefSymbolTable = ParentRefSymbolTable("/path/to/some/file/foo/bar/fizz/buzz.wacc")
    private val errBuilder = SemanticErrorMessageBuilder().provideStart(0, 0).setLineText("")

    @Test
    fun canDeclareInts() {
        parentRefSymbolTable.declare("x", WInt(), errBuilder)
    }

    @Test
    fun canDeclareStrings() {
        parentRefSymbolTable.declare("s", WStr(), errBuilder)
    }


    @Test
    fun canGetInts() {
        parentRefSymbolTable.declare("x", WInt(), errBuilder)
        parentRefSymbolTable.get("x", errBuilder)

    }

    @Test
    fun canGetStrings() {
        parentRefSymbolTable.declare("x", WStr(), errBuilder)
        parentRefSymbolTable.get("x", errBuilder)
    }

    @Test
    fun canReassignInt() {
        parentRefSymbolTable.declare("x", WInt(), errBuilder)
        parentRefSymbolTable.reassign("x", WInt(), errBuilder)

    }

    @Test
    fun canReassignString() {
        parentRefSymbolTable.declare("x", WStr(), errBuilder)
        parentRefSymbolTable.reassign("x", WStr(), errBuilder)
    }

    @Test
    fun reassigningIntWithStringThrowsSemanticException() {
        parentRefSymbolTable.declare("x", WInt(), errBuilder)
        try {
            parentRefSymbolTable.reassign("x", WStr(), errBuilder)
            fail() // Should not get here
        } catch (e: SemanticException) {
            println(e)
        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun gettingIntAsStringThrowsSemanticException() {
        parentRefSymbolTable.declare("x", WInt(), errBuilder)
        try {
            parentRefSymbolTable.getAndCast<WStr>("x", errBuilder)
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
            parentRefSymbolTable.get("x", errBuilder)
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
        child.declare("x", WInt(), errBuilder)
        try {
            st.get("x", errBuilder)
            fail() // Should not get here
        } catch (e: SemanticException) {
            println(e)
        } catch (e: Exception) {
            fail()
        }
    }

    @Test
    fun canDeclareArrays() {
        parentRefSymbolTable.declare("array", WArray(WUnknown), errBuilder)
    }

    @Test
    fun canDeclareArrayOfBaseTypes() {
        parentRefSymbolTable.declare("array_1", WArray(WInt()), errBuilder)
        parentRefSymbolTable.declare("array_2", WArray(WStr()), errBuilder)
        parentRefSymbolTable.declare("array_3", WArray(WBool()), errBuilder)
        parentRefSymbolTable.declare("array_4", WArray(WChar()), errBuilder)
    }

    @Test
    fun canDeclareArrayOfPairs() {
        val pairUnknownUnknown = WPair(WUnknown, WUnknown)
        val pairStringInt = WPair(WStr(), WInt())
        val pairStringPairStringInt = WPair(WStr(), pairStringInt)
        parentRefSymbolTable.declare("array_1", pairUnknownUnknown, errBuilder)
        parentRefSymbolTable.declare("array_2", pairStringInt, errBuilder)
        parentRefSymbolTable.declare("array_3", pairStringPairStringInt, errBuilder)
    }

    @Test
    fun canDeclareArrayOfArrays() {
        val aI = WArray(WInt())
        val aaI = WArray(aI)
        val aaaI = WArray(aaI)
        parentRefSymbolTable.declare("array_1", aI, errBuilder)
        parentRefSymbolTable.declare("array_2", aaI, errBuilder)
        parentRefSymbolTable.declare("array_3", aaaI, errBuilder)
    }

    @Test
    fun canAssignValuesInBaseTypeArray() {
        val arrayName = "array"
        // invalid array indexing is a run-time error
        val indices: Array<WInt> = arrayOf(WInt())
        parentRefSymbolTable.declare(arrayName, WArray(WInt()), errBuilder)
        parentRefSymbolTable.reassign(arrayName, indices, WInt(), errBuilder)
    }

    @Test
    fun canAssignValuesInPairArrays() {
        val arrayName = "array"
        val pairStringPairUnknownUnknown = WPair(WStr(), WPair(WUnknown, WUnknown))
        // invalid array indexing is a run-time error
        val indices: Array<WInt> = arrayOf(WInt())
        parentRefSymbolTable.declare(arrayName, WArray(pairStringPairUnknownUnknown), errBuilder)
        parentRefSymbolTable.reassign(arrayName, indices, pairStringPairUnknownUnknown, errBuilder)
        try {
            parentRefSymbolTable.reassign(arrayName, indices, WInt(), errBuilder)
        } catch (e: SemanticException) {
            println(e)
        }
    }
}