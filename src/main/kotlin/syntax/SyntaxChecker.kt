package syntax

import antlr.WACCParser
import ast.Stat
import ast.statement.*
import symbolTable.SymbolTable
import utils.ExitCode
import utils.PositionedError
import utils.SyntaxErrorMessageBuilder
import kotlin.system.exitProcess

class SyntaxChecker {
    companion object {
        /**
         * Ensures that the integer literal is within the bounds (-2^32, 2^32 - 1)
         */
        fun assertIntFitsTheRange(ctx: WACCParser.LiteralIntegerContext, st: SymbolTable) {
            try {
                Integer.parseInt(ctx.text)
            } catch (e: java.lang.NumberFormatException) {
                SyntaxErrorMessageBuilder()
                    .provideStart(PositionedError(ctx))
                    .setLineTextFromSrcFile(st.srcFilePath)
                    .appendCustomErrorMessage("Attempted to parse a very big int ${ctx.text}!")
                    .buildAndPrint()
                exitProcess(ExitCode.SYNTAX_ERROR)
            }
        }

        /**
         * Checks whether the given statement has a proper return statement by matching patterns recursively
         * @param stat : statement to be checked
         * @param inOuterFuncScope : examines the scope to check context
         **/
        private fun hasReturn(stat: Stat, inOuterFuncScope: Boolean): Boolean {
            return when (stat) {
                is ReturnStat -> true
                is ExitStat -> true
                is JoinStat -> unreachableCodeCheck(stat, inOuterFuncScope)
                is IfThenStat -> hasReturn(stat.thenStat, false)
                        && hasReturn(stat.elseStat, false)
                is WhileStat -> hasReturn(stat.doBlock, false)
                else -> false
            }
        }

        private fun unreachableCodeCheck(stat: JoinStat, inOuterFuncScope: Boolean): Boolean {
            val errorCondition= hasReturn(stat.first, true)
                    && !hasReturn(stat.second, false)
                    && inOuterFuncScope
            if (!errorCondition) {
                return hasReturn(stat.second, true)
            }
            println("Should not have return before another non-return statement.")
            exitProcess(ExitCode.SYNTAX_ERROR)
        }

        fun checkFunctionHavingReturn(body: Stat, identifier: String) {
            if (!hasReturn(body, true)) {
                println("Function $identifier does not return on every branch.")
                exitProcess(ExitCode.SYNTAX_ERROR)
            }
        }
    }
}