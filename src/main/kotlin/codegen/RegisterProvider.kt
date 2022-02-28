package codegen

import instructions.aux.Register

/**
 * Temporary solution for register allocation
 * Keeps track of callee saved registers
 */
class RegisterProvider {

    private var tracker: Int = FIRST_CALLEE_SAVE_REG

    fun get(): Register {
        if (tracker > LAST_CALLEE_SAVE_REG) {
            throw Exception("Ran out of callee-saved registers")
        }
        return Register("r${tracker++}")
    }

    fun ret() {
        tracker--
        if (tracker < FIRST_CALLEE_SAVE_REG) {
            throw Exception("RegisterProvider tracker misuse")
        }
    }

    companion object {
        private const val FIRST_CALLEE_SAVE_REG = 4
        private const val LAST_CALLEE_SAVE_REG = 11
    }
}