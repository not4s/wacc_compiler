package instructions

class ADD: WInstruction {

    private var rDest: Register? = null
    private var rSrc: Register?  = null
    private var op2: Operand2?   = null
    private var shiftVal: Int?   = null
    var flagSet: Boolean         = false

    constructor(rDest: Register, rSrc: Register, op2: Operand2) {
        this.rDest = rDest
        this.rSrc  = rSrc
        this.op2   = op2
    }

    constructor(rDest: Register, rSrc: Register, op2: Operand2, shiftVal: Int) {
        this.rDest    = rDest
        this.rSrc     = rSrc
        this.op2      = op2
        this.shiftVal = shiftVal
    }

    override fun toString(): String {
        var sb = "ADD"
        if(flagSet) sb.plus("S")
        sb.plus(" " + rDest + ", " + rSrc + ", " + op2)
        if(shiftVal != null) sb.plus(", LSL #" + shiftVal)
        return sb
    }
}