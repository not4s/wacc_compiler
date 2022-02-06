package utils

class Debug {
    companion object {
        const val FLAG_ARG = "--debug_mode"
        var isInDebugMode: Boolean = false

        fun infoLog(msg: String) {
            if (isInDebugMode) {
                println("INFO: $msg")
            }
        }

        fun errorLog(msg: String) {
            if (isInDebugMode) {
                println("ERROR: $msg")
            }
        }
    }
}