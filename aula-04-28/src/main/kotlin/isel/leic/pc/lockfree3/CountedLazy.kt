package isel.leic.pc.lockfree3

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.function.Supplier

class UnsafeCountedLazy<T>(
    val supplier: Supplier<T>, val closer: Consumer<T>) {

    private var counter = -1
    private var value: T? = null

    fun acquire(): T? {
        check(counter != 0) { "Object is closed" }
        if (counter == -1) {
            counter = 1
            value = supplier.get()
        } else {
            counter += 1
            while (value == null) Thread.yield()
        }
        return value
    }

    fun release() {
        check(counter > 0)
        if (--counter == 0) {
            closer.accept(value!!)
        }
    }

}

class CountedLazy<T>(val supplier : Supplier<T>,
                    val closer : Consumer<T>) {


    fun acquire() : T {
        TODO()
    }

    fun release() {
       TODO()
    }
}