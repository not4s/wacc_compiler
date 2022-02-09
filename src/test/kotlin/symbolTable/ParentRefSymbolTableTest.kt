package symbolTable

import org.junit.Test
import utils.SemanticException
import waccType.*
import kotlin.test.fail

class ParentRefSymbolTableTest {
    @Test
    fun canDeclareInts() {
        val parentRefSymbolTable = ParentRefSymbolTable()
        parentRefSymbolTable.declare("x", WInt())
    }

    @Test
    fun canDeclareStrings() {
        val parentRefSymbolTable = ParentRefSymbolTable()
        parentRefSymbolTable.declare("s", WStr())
    }


    @Test
    fun canGetInts() {
        val parentRefSymbolTable = ParentRefSymbolTable()
        parentRefSymbolTable.declare("x", WInt())
        parentRefSymbolTable.get("x")

    }

    @Test
    fun canGetStrings() {
        val parentRefSymbolTable = ParentRefSymbolTable()
        parentRefSymbolTable.declare("x", WStr())
        parentRefSymbolTable.get("x")
    }

    @Test
    fun canReassignInt() {
        val parentRefSymbolTable = ParentRefSymbolTable()
        parentRefSymbolTable.declare("x", WInt())
        parentRefSymbolTable.reassign("x", WInt())

    }

    @Test
    fun canReassignString() {
        val parentRefSymbolTable = ParentRefSymbolTable()
        parentRefSymbolTable.declare("x", WStr())
        parentRefSymbolTable.reassign("x", WStr())
    }

    @Test
    fun reassigningIntWithStringThrowsSemanticException() {
        val parentRefSymbolTable = ParentRefSymbolTable()
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
        val parentRefSymbolTable = ParentRefSymbolTable()
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
        val parentRefSymbolTable = ParentRefSymbolTable()
        try {
            parentRefSymbolTable.get("x")
            fail() // Should not get here
        } catch (e: SemanticException) {
            println(e)
        } catch (e: Exception) {
            fail()
        }
    }

//    @Test
//    fun reassigningUndeclaredVariableThrowsSemanticException() {
//        val parentRefSymbolTable = ParentRefSymbolTable()
//        try {
//            parentRefSymbolTable.reassign("x", WInt(1337))
//            fail() // Should not get here
//        } catch (e: SemanticException) {
//            println(e)
//        } catch (e: Exception) {
//            fail()
//        }
//    }

    @Test
    fun canCreateChildScope() {
        val st = ParentRefSymbolTable()
        st.createChildScope()
    }

    @Test
    fun declaringVariableInChildScopeDoesNotCreateItInParent() {
        val st = ParentRefSymbolTable()
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
        val parentRefSymbolTable = ParentRefSymbolTable()
        parentRefSymbolTable.declare("array", WArray(WUnknown()))
    }

    @Test
    fun canDeclareArrayOfBaseTypes() {
        val parentRefSymbolTable = ParentRefSymbolTable()
        parentRefSymbolTable.declare("array_1", WArray(WInt()))
        parentRefSymbolTable.declare("array_2", WArray(WStr()))
        parentRefSymbolTable.declare("array_3", WArray(WBool()))
        parentRefSymbolTable.declare("array_4", WArray(WChar()))
    }

    @Test
    fun canDecalreArrayOfPairs() {
        val parentRefSymbolTable = ParentRefSymbolTable()
        val pU_U = WPair(WUnknown(), WUnknown())
        val pS_I = WPair(WStr(), WInt())
        val pS_p_S_I = WPair(WStr(), pS_I)
        parentRefSymbolTable.declare("array_1", pU_U)
        parentRefSymbolTable.declare("array_2", pS_I)
        parentRefSymbolTable.declare("array_3", pS_p_S_I)
    }

    @Test
    fun canDecalreArrayOfArrays() {
        val parentRefSymbolTable = ParentRefSymbolTable()
        val aI = WArray(WInt())
        val aaI = WArray(aI)
        val aaaI = WArray(aaI)
        parentRefSymbolTable.declare("array_1", aI)
        parentRefSymbolTable.declare("array_2", aaI)
        parentRefSymbolTable.declare("array_3", aaaI)
    }

    @Test
    fun canAssignValuesInBaseTypeArray() {
        val parentRefSymbolTable = ParentRefSymbolTable()
        val arrayName = "array"
        // invalid array indexing is a run-time error
        val indices: Array<WInt> = arrayOf(WInt())
        parentRefSymbolTable.declare(arrayName, WArray(WInt()))
        parentRefSymbolTable.reassign(arrayName, indices, WInt())
    }

    @Test
    fun canAssignValuesInPairArrays() {
        val parentRefSymbolTable = ParentRefSymbolTable()
        val arrayName = "array"
        val pS_p_S_I = WPair(WStr(), WPair(WUnknown(), WUnknown()))
        // invalid array indexing is a run-time error
        val indices: Array<WInt> = arrayOf(WInt())
        parentRefSymbolTable.declare(arrayName, WArray(pS_p_S_I))
        parentRefSymbolTable.reassign(arrayName, indices, pS_p_S_I)
        try { parentRefSymbolTable.reassign(arrayName, indices, WInt()) }
        catch (e: SemanticException) { println(e) }
    }
}