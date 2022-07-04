package isel.leic.pc

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import java.util.function.Supplier


class SafeCountedLazy<T>(
    private val supplier: Supplier<T>,
    private val closer: Consumer<T>) {

    private var counter = AtomicInteger(-1)

    @Volatile
    private var value: T? = null


    fun acquire(): T {

        do {
            val obsCounter = counter.get()
            check(obsCounter != 0) { "Object is closed" }
            if (obsCounter == -1) {
                if (counter.compareAndSet(-1, 1)) {
                    value = supplier.get()
                    return value!!
                }
            }
            else {
                if (counter.compareAndSet(obsCounter, obsCounter +1)) {
                    while (value == null) Thread.yield()
                    return value!!
                }
            }
        }
        while(true)
    }

    fun release() {
        do {
            val obsCounter = counter.get()
            check(obsCounter > 0)
            if (counter.compareAndSet(obsCounter, obsCounter-1)) {
                if (obsCounter == 1)
                    closer.accept(value!!)
            }
        }
        while(true)

    }
}