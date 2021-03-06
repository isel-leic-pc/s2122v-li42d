package isel.leic.pc.monitors

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

class SimpleSemaphore(private var permits: Int) {
    val mutex = ReentrantLock()
    val hasPermits : Condition = mutex.newCondition()

    /*
      As we will see in the next lecture this simple solution may not
      work well if the await on the condition throws a InterruptedException.
      since a notification can be lost
     */
    @Throws(InterruptedException::class)
    fun acquire() {
        mutex.withLock {
            /*
               Note that, due to barging we should revaluate the
               wait condition, the while is real necessary
             */
            try {
                while (permits == 0) {
                    hasPermits.await()
                }
                permits -= 1
            }
            catch(e : InterruptedException) {
                /*
                if (permits > 0) {
                    Thread.currentThread().interrupt();
                    permits--;
                    return;
                }
                */
                if (permits > 0) {
                    hasPermits.signal()
                }
                throw e
            }
        }
    }

    fun release() {
        mutex.withLock {
            permits += 1
            hasPermits.signal()
        }
    }

}