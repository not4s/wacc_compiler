package codegen

import instructions.WInstruction
import instructions.misc.Label

class FunctionPool {
    private val contents = mutableListOf<List<WInstruction>>()
    private val declaredFuncs = mutableSetOf<String>()

    // Returns True if the function declaration was added, otherwise returns False.
    fun add(func: List<WInstruction>): Boolean {
        // Empty: no add
        if (func.isEmpty()) {
            return false
        }
        // Make sure the first element is a label, otherwise don't add.
        if (func.first() !is Label) {
            return false
        }
        // If already defined, don't add
        if ((func.first() as Label).label in declaredFuncs) {
            return false
        }
        // Finally, add
        contents.add(func)
        declaredFuncs.add((func.first() as Label).label)
        return true
    }

    // Returns all WInstructions as one list
    fun flatten() : List<WInstruction> {
        return contents.flatten()
    }
}
