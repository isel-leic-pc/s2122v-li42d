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

/**
 * A safe version for the previoys UnsafeCountedLazy try
 */
class SafeCountedLazy<T>(val supplier : Supplier<T>,
                    val closer : Consumer<T>) {

    private val counter = AtomicInteger(-1)

    // must be volatile to guarantee correct publication
    // since no happens-before exist associated to counter
    @Volatile
    private var value : T? = null

    fun acquire() : T {
        do {
            val obsCounter = counter.get()
            check(obsCounter != 0) { "Object is closed" }
            if (obsCounter == -1) {
                if (counter.compareAndSet(obsCounter, 1)) {
                    return supplier.get().also() { value = it }
                }
            }
            else {
                if (counter.compareAndSet(obsCounter, obsCounter +1)) {
                    while(value == null) Thread.yield()
                    return value!!
                }
            }
        }
        while(true);
    }

    fun release() {
        do {
            val obsCounter = counter.get()
            check(obsCounter > 0)
            if (counter.compareAndSet(obsCounter, obsCounter -1 )) {
                if (obsCounter - 1 == 0)
                    closer.accept(value!!)
            }
        }
        while(true)
    }
}