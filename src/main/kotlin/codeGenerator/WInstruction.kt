package codeGenerator

// TODO: decide interface vs abstract class, let's go for interface for now
// the interface of all types of instructions
interface WInstruction {
    override fun toString(): String
}