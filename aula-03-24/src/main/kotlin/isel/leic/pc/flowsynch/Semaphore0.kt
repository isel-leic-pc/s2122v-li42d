package isel.leic.pc.flowsynch

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Semaphore0(private var permits : Int) {
    val mutex = ReentrantLock()
    val hasPermits : Condition = mutex.newCondition()

    fun acquire(units : Int) {
        mutex.withLock {
            while (permits < units) {
                hasPermits.await()
            }
            permits -= units
        }
    }

    fun release(units : Int) {
        mutex.withLock {
            permits += units
            hasPermits.signalAll()
        }
    }

}