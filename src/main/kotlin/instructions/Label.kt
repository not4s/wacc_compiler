package instructions

class Label(val label: String): WInstruction {
    
    override fun toString(): String{
        return label + ":"
    }
}