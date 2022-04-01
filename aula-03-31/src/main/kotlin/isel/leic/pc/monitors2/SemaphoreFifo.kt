package isel.leic.pc.monitors2

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

class SemaphoreFifo {
    val mutex = ReentrantLock()
    val hasPermits : Condition = mutex.newCondition()

    @Throws(InterruptedException::class)
    fun acquire(units : Int, timeout : Duration) {
        mutex.withLock {

        }
    }

    fun release(units : Int) {
        mutex.withLock {

        }
    }

    @Throws(InterruptedException::class)
    fun acquire(units : Int) = acquire(units, Duration.INFINITE)
}