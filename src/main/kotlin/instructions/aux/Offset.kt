package instructions.aux

import instructions.Loadable

interface Offset : Loadable

/**
 * Immediate offset for single word, such as:
 * 1) LDR{type}{cond} Rt, [Rn {, #offset}] ;  :: immediate offset :: preIndexed = null
 * 2) LDR{type}{cond} Rt, [Rn, #offset]! ;    :: pre-indexed      :: preIndexed = true
 * 3) LDR{type}{cond} Rt, [ Rn], #offset ;    :: post-indexed     :: preIndexed = false
 *
 * @param preIndexed is a nullable Boolean, which determines the type of the
 * instruction as described above.
 */
class ImmediateOffset(
    private val baseRegister: Register,
    private val offset: Int? = null,
    private val preIndexed: Boolean? = null
) : Offset {
    override fun toString(): String {
        offset ?: return "[$baseRegister]"
        return when (preIndexed) {
            null -> "[$baseRegister, #$offset]"
            true -> "[$baseRegister, #$offset]!"
            false -> "[$baseRegister], #$offset"
        }
    }
}

/**
 * Register offset for single word, such as:
 * 1) LDR{type}{cond} Rt, [Rn, ±Rm {, shift}]  ; register offset
 * 2) LDR{type}{cond} Rt, [Rn, ±Rm {, shift}]! ; pre-indexed ; ARM only
 * 3) LDR{type}{cond} Rt, [ Rn], ±Rm {, shift}  ; post-indexed ; ARM only
 *
 * @param preIndexed is a nullable Boolean, which determines the type of the
 * instruction as described above.
 */
class RegisterOffset(
    private val baseRegister: Register,
    private val offsetRegister: Register,
    private val positiveReg: Boolean,
    private val shift: Int? = null,
    private val preIndexed: Boolean? = null
) : Offset {
    override fun toString(): String {
        val plusMinus = if (positiveReg) "+$offsetRegister" else "-$offsetRegister"
        val extras = plusMinus + (shift?.let { ", $shift" } ?: "")
        return when (preIndexed) {
            null -> "[$baseRegister, $extras]"
            true -> "[$baseRegister, $extras]!"
            false -> "[$baseRegister], $extras"
        }
    }
}
