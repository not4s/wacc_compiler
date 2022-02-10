package utils

class ExitCode(val code: Int) {
    companion object {
        const val SYNTAX_ERROR: Int = 100
        const val SEMANTIC_ERROR: Int = 200
    }
}