package isel.leic.pc

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class MutableInt(var value : Int = 0) {
    val mutex = ReentrantLock()

    fun increment()  {
        mutex.withLock {
            ++value
        }
    }

    fun add(mutInt : MutableInt) = MutableInt(value + mutInt.value)
}

class MutableInt0(var value : Int = 0) {

    fun increment()  {
        ++value
    }

    fun add(mutInt : MutableInt) = MutableInt(value + mutInt.value)
}

class MutableInt2(var initial : Int = 0) {

    val atomic = AtomicInteger(initial)

    fun increment()  {
        atomic.incrementAndGet()
    }

    val value : Int
        get() = atomic.get()

    fun add(mutInt : MutableInt) = MutableInt(value + mutInt.value)
}