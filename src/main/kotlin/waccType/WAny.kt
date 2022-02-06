package waccType

abstract class WAny {
    abstract val value : Any?

    fun sameTypeAs(other: WAny) : Boolean {
        return this.javaClass == other.javaClass
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WAny) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }
}