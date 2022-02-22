package utils

class ExitCode(val code: Int) {
    companion object {
        const val SYNTAX_ERROR: Int = 100
        const val SEMANTIC_ERROR: Int = 200
    }
}
const val ARM_HELLO_WORLD_PROGRAM: String =
        ".data\n" +
        "msg: \n" +
        "    .ascii \"Hello World\\n\"\n" +
        ".text\n" +
        ".globl main\n" +
        "main: \n" +
        "    mov r0, #1\n" +
        "    ldr r1, =msg \n" +
        "    mov r2, #12    \n" +
        "    mov r7, #4 \n" +
        "    swi #0\n" +
        "    mov r0, #0\n" +
        "    mov r7, #1\n" +
        "    swi #0\n"