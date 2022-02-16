package syntax

import antlr.WACCParser
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

    }
}