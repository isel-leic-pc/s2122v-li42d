package isel.leic.pc


val num by lazy {
    3
}

class MyLazy<T>(private val factory : () -> T ) {
    var value : T? = null
    val mutex = Any()
    fun get() : T {
        synchronized(mutex) {
            if (value == null)
                value = factory()
            return value!!
        }

    }

}

