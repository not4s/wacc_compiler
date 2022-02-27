package instructions.aux

class Immediate(val value: Int) {
    override fun toString(): String {
        return "=$value"
    }
}