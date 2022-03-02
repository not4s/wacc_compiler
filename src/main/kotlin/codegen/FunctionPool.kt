package codegen

import instructions.WInstruction
import instructions.misc.Label

class FunctionPool {
    private val contents = mutableListOf<List<WInstruction>>()
    private val declaredFuncs = mutableSetOf<String>()

    private var labelIndex = 0

    // gives a new label name to a given
    fun getAbstractLabel(): String {
        return "L${labelIndex++}"
    }

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
        if (containsFunc(func.first() as Label)) {
            return false
        }
        // Finally, add
        contents.add(func)
        declaredFuncs.add((func.first() as Label).label)
        return true
    }

    // Returns all WInstructions as one list
    fun flatten(): List<WInstruction> {
        return contents.flatten()
    }

    fun containsFunc(label: String): Boolean {
        return label in declaredFuncs
    }

    fun containsFunc(label: Label): Boolean {
        return containsFunc(label.label)
    }
}
