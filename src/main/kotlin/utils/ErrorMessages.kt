package utils

import WACCType.WAny
import org.antlr.v4.runtime.ParserRuleContext
import semantic.ExprType
import kotlin.system.exitProcess

fun raiseTypeErrorAndExit(ctx: ParserRuleContext?, expectedType: WAny?, actualType: WAny?) {
    ctx?.run {
        println("Line ${ctx.start.line}: Invalid operand expression type\n" +
                "Expected: $expectedType, got: $actualType\"")
        exitProcess(ExitCode.SEMANTIC_ERROR)
    }
    raiseNullContextError()
}

fun raiseNullContextError() {
    println("Null Context Error")
    exitProcess(ExitCode.UNKNOWN_ERROR)
}

fun raiseSemanticErrorAndExit() {
    println("Semantic error!")
    exitProcess(ExitCode.SEMANTIC_ERROR)
}

class SemanticException(private val reason: String) : Exception() {
    override val message: String
        get() = "Semantic error!\n$reason"
}