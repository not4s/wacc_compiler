package symbolTable

import instructions.WInstruction
import instructions.misc.DataDeclaration
import instructions.misc.Register
import utils.SemanticErrorMessageBuilder
import utils.SemanticException
import waccType.*

abstract class SymbolTable(
    var isGlobal: Boolean,
    val srcFilePath: String,
) {
    abstract fun get(symbol: String, errorMessageBuilder: SemanticErrorMessageBuilder): WAny

    abstract fun get(
        arrSym: String,
        indices: Array<WInt>,
        errorMessageBuilder: SemanticErrorMessageBuilder,
    ): WAny

    abstract fun getMap(): Map<String, WAny>

    inline fun <reified T : WAny> getAndCast(
        symbol: String,
        errorMessageBuilder: SemanticErrorMessageBuilder,
    ): T {
        val value = this.get(symbol, errorMessageBuilder)
        if (value is T) {
            return value
        } else {
            throw SemanticException("Attempted to cast variable $value(${value::class.simpleName}) to ${T::class.simpleName}")
        }
    }

    abstract fun declare(
        symbol: String,
        value: WAny,
        errorMessageBuilder: SemanticErrorMessageBuilder,
    )

    /**
     * Reassignment for base types
     */
    abstract fun reassign(
        symbol: String,
        value: WAny,
        errorMessageBuilder: SemanticErrorMessageBuilder,
    )

    /**
     * Reassignment for arrays
     */
    abstract fun reassign(
        arrSym: String,
        indices: Array<WInt>,
        value: WAny,
        errorMessageBuilder: SemanticErrorMessageBuilder,
    )

    /**
     * Reassignment for pairs
     */
    abstract fun reassign(
        pairSym: String,
        fst: Boolean,
        value: WAny,
        errorMessageBuilder: SemanticErrorMessageBuilder,
    )

    abstract fun createChildScope(): SymbolTable

    fun typeToByteSize(value: WAny): Int {
        // Bools, chars are 1 byte
        // Ints, string pointers, array pointers are 4
        // Pairs are stored in a pointer, 4 bytes.
        return when (value) {
            is WBool, is WChar -> 1
            is WInt, is WStr, is WArray -> 4
            is WPair, is IncompleteWPair -> 4
            else -> 0
        }
    }

    val totalByteSize: Int
        get() {
            return getMap().values.sumOf { typeToByteSize(it) }
        }

    // Get the instruction to assign this variable. Must have already been declared (ie. existing in map) during first pass.
    abstract fun asmAssign(
        symbol: String,
        fromRegister: Register,
        data: DataDeclaration,
        type: WAny?
    ): List<WInstruction>

    abstract fun asmAssign(
        arrSym: String,
        indices: Array<WInt>, fromRegister: Register,
        data: DataDeclaration,
    ): List<WInstruction>

    abstract fun asmAssign(
        pairSym: String,
        fst: Boolean, fromRegister: Register,
        data: DataDeclaration,
    ): List<WInstruction>

    abstract fun asmGet(symbol: String, toRegister: Register, data: DataDeclaration): List<WInstruction>
}