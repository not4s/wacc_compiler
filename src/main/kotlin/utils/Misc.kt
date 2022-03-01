package utils

class ExitCode(val code: Int) {
    companion object {
        const val SYNTAX_ERROR: Int = 100
        const val SEMANTIC_ERROR: Int = 200
    }
}

fun btoi(b: Boolean): Int = if (b) 1 else 0
