package ast

import antlr.WACCParser
import org.antlr.v4.runtime.Token

/**
 * Types of the different binary operations
 **/
enum class BinOperator {
    MUL, DIV, MOD, ADD, SUB, GT, GEQ, LT, LEQ, EQ, NEQ, AND, OR;

    companion object {

        fun isForInt(it: BinOperator): Boolean = when (it) {
            MUL, DIV, MOD, ADD, SUB -> true
            else -> false
        }

        fun isOrdering(it: BinOperator): Boolean = when (it) {
            GT, GEQ, LT, LEQ -> true
            else -> false
        }

        fun isForAnyType(it: BinOperator): Boolean = when (it) {
            EQ, NEQ -> true
            else -> false
        }

        fun isForBool(it: BinOperator): Boolean = when (it) {
            AND, OR -> true
            else -> false
        }

        fun fromWACCParserContextBinOp(binOp: Token): BinOperator = when (binOp.type) {
            WACCParser.OP_MULT -> MUL
            WACCParser.OP_DIV -> DIV
            WACCParser.OP_MOD -> MOD
            WACCParser.OP_ADD -> ADD
            WACCParser.OP_SUBT -> SUB
            WACCParser.OP_GT -> GT
            WACCParser.OP_GEQ -> GEQ
            WACCParser.OP_LT -> LT
            WACCParser.OP_LEQ -> LEQ
            WACCParser.OP_EQ -> EQ
            WACCParser.OP_NEQ -> NEQ
            WACCParser.OP_AND -> AND
            WACCParser.OP_OR -> OR
            else -> throw Exception("Unknown binary operand")
        }
    }
}

/**
 * Types of the different unary operations
 **/
enum class UnOperator {
    NOT, ORD, CHR, LEN, SUB;

    companion object {
        fun fromWACCParserContextUnOp(unOp: Token): UnOperator = when (unOp.type) {
            WACCParser.OP_NOT -> NOT
            WACCParser.OP_ORD -> ORD
            WACCParser.OP_CHR -> CHR
            WACCParser.OP_LEN -> LEN
            WACCParser.OP_SUBT -> SUB
            else -> throw Exception("Unknown unary operand")
        }
    }
}