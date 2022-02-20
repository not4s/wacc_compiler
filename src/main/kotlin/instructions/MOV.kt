package instructions

class MOV: WInstruction {

    enum class Condition {
        LT, LTE, EQ, GT, GTE, NE
    }

    private var rDest: Register? = null
    private var op2: Operand2?   = null
    var cond: Condition?         = null

    constructor(rDest: Register, op2: Operand2) {
        this.rDest = rDest
        this.op2   = op2
    }

    constructor(rDest: Register, op2: Operand2, cond: Condition) {
        this.rDest = rDest
        this.op2   = op2
        this.cond  = cond
    }

    override fun toString(): String {
        var sb = "MOV"
        if(cond != null) sb.plus(cond)
        sb.plus(" " + rDest + ", " + op2)
        return sb
    }
}