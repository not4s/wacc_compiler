package waccType

class WInt(override val value: Int?) : WAny() {
    constructor() : this(null)
}

class WBool(override val value: Boolean?) : WAny() {
    constructor() : this(null)
}

class WChar(override val value: Char?) : WAny() {
    constructor() : this(null)
}

class WStr(override val value: String?) : WAny() {
    constructor() : this(null)
}

class WPair<T : WAny, S : WAny>(override val value: Pair<T, S>?) : WAny() {
    constructor() : this(null)
}

class WArray<T : WAny>(override val value: Array<T>?) : WAny() {
    constructor() : this(null)
}
//class WFunc<T: WAny>

