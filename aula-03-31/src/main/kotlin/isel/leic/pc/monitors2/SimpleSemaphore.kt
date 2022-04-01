package isel.leic.pc.monitors2

import isel.leic.pc.utils.*

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

class SimpleSemaphore(private var permits: Int) {
    val mutex = ReentrantLock()
    val hasPermits : Condition = mutex.newCondition()

    /*
      As we see in the next lecture this simple solution may not
      work well if the await on the condition throws a InterruptedException.
      Since a notification can be lost
     */
    fun acquire(timeout: Duration) : Boolean{
        mutex.withLock {
            if (permits > 0) {
                permits--
                return true
            }
            if (timeout.isZero) {
                return false
            }
            /*
               Note that, due to barging we should revaluate the
               wait condition, the while is real necessary
             */
            val dueTime = timeout.dueTime()
            do {
                try {
                    hasPermits.await(dueTime)
                    if (permits > 0) {
                        permits--
                        return true
                    }
                    if (dueTime.isPast) return false
                }
                catch(e: InterruptedException) {
                    if (permits > 0) hasPermits.signal()
                    throw e
                }
            }
            while(true);
        }
    }

    /*
      As we see in the next lecture this simple solution may not
      work well if the await on the condition throws a InterruptedException.
      Since a notification can be lost
     */
    fun acquire2(timeout: Duration) : Boolean{
        mutex.withLock {
            if (permits > 0) {
                permits--
                return true
            }
            if (timeout.isZero) {
                return false
            }
            /*
               Note that, due to barging we should revaluate the
               wait condition, the while is real necessary
             */
            val tm = MutableTimeout(timeout)
            do {
                try {
                    hasPermits.await(tm)
                    if (permits > 0) {
                        permits--
                        return true
                    }
                    if (tm.elapsed) return false
                }
                catch(e: InterruptedException) {
                    if (permits > 0) hasPermits.signal()
                    throw e
                }
            }
            while(true);
        }
    }

    fun release() {
        mutex.withLock {
            permits += 1
            hasPermits.signal()
        }
    }

}