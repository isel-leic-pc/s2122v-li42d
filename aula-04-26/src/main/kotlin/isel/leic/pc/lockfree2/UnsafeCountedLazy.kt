package isel.leic.pc.lockfree2

import java.util.function.Consumer
import java.util.function.Supplier


class UnsafeCountedLazy<T>(private val supplier: Supplier<T>,
                           private val closer: Consumer<T>) {

    private var counter = -1

    private var value: T? = null


    fun acquire(): T {
        check(counter != 0) { "Object is closed" }
        if (counter == -1) {
            counter = 1
            value = supplier.get()
        } else {
            counter += 1
            while (value == null) Thread.yield()
        }
        return value!!
    }

    fun release() {
        check(counter > 0)
        if (--counter == 0) {
            closer.accept(value!!)
        }
    }

}