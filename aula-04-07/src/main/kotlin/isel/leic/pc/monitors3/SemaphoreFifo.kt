package isel.leic.pc.monitors3

import isel.leic.pc.utils.await
import isel.leic.pc.utils.dueTime
import isel.leic.pc.utils.isPast
import isel.leic.pc.utils.isZero
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

class SemaphoreFifo(private var permits : Int) {
    private val monitor = ReentrantLock()

    private val pendingAcquires = mutableListOf<PendingAcquire>()

    class PendingAcquire(val units: Int, val condition: Condition) {
        var done : Boolean = false
    }


    @Throws(InterruptedException::class)
    fun acquire(units : Int, timeout : Duration) : Boolean {
        monitor.withLock {
            // fast path
            if (pendingAcquires.size == 0 && permits >= units) {
                permits-= units
                return true
            }
            if (timeout.isZero) return false
            // wait path
            val waiter = PendingAcquire(units, monitor.newCondition())
            val dueTime = timeout.dueTime()
            pendingAcquires.add(waiter)
            do {
                try {
                    waiter.condition.await(dueTime)
                    if (waiter.done) return true
                    if (dueTime.isPast) {
                        pendingAcquires.remove(waiter)
                        return false
                    }
                }
                catch(e: InterruptedException) {
                    pendingAcquires.remove(waiter)
                    throw e
                }
            }
            while(true)

        }
    }

    private fun notifyWaiters() {
        while(pendingAcquires.size > 0 &&
              permits >= pendingAcquires.first().units) {
            val waiter = pendingAcquires.removeFirst()
            permits -= waiter.units
            waiter.done = true
            waiter.condition.signal()
        }

    }

    fun release(units : Int) {
        monitor.withLock {
            permits += units
            notifyWaiters()
        }
    }

    @Throws(InterruptedException::class)
    fun acquire(units : Int) = acquire(units, Duration.INFINITE)
}