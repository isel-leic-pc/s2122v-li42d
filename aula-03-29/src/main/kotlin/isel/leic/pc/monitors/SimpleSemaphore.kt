package isel.leic.pc.monitors

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SimpleSemaphore(private var permits: Int) {
    val mutex = ReentrantLock()
    val hasPermits : Condition = mutex.newCondition()

    /*
      As we will see in the next lecture this simple solution may not
      work well if the await on the condition throws a InterruptedException.
      since a notification can be lost
     */
    fun acquire() {
        mutex.withLock {
            /*
               Note that, due to barging we should revaluate the
               wait condition, the while is real necessary
             */
            while (permits == 0) {
                hasPermits.await()
            }
            permits -= 1
        }
    }

    fun release() {
        mutex.withLock {
            permits += 1
            hasPermits.signal()
        }
    }

}