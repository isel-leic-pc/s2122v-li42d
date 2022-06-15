package data

data class T(val t : Int) {
    companion object {
        fun plus(t1: T, t2: T) = T(t1.t + t2.t)
    }

}