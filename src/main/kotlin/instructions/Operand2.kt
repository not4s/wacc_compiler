package instructions

class Operand2 {
    
    private var reg: Register? = null
    private var offset: Int?   = null
    private var imm: Int?      = null
    private var label: String? = null

    constructor(reg: Register) {
        this.reg = reg
    }
    
    constructor(reg: Register, offset: Int) {
        this.reg    = reg
        this.offset = offset
    }

    constructor(imm: Int) {
        this.imm = imm
    }

    constructor(label: String) {
        this.label = label
    }

    fun getReg(): Register? {
        return this.reg
    }

    fun getOffset(): Int? {
        return this.offset
    }

    override fun toString(): String {
        var sb: String = ""
        when {
            reg != null && offset != null ->
                sb = "[" + reg + ", #" + offset + "]"
            reg != null ->
                sb = "[" + reg + "]"
            imm != null ->
                sb = "#, " + imm
            label != null ->
                sb = "=" + label
        }
        return sb
    }
}