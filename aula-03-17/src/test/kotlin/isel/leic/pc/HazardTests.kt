package isel.leic.pc

import org.junit.Assert
import org.junit.Test
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

class HazardTests {

     @Test
     fun multiple_threads_increment_test() {
         var count = 0

         val NTHREADS = 2
         val NITERS   = 1000000

         val threads = ArrayList<Thread>()
         repeat(NTHREADS) {
             val t = thread {
                 repeat(NITERS) {
                     synchronized(this) {
                         val res = count
                         count = res + 1
                     }
                 }
             }
             threads.add(t)
         }

         for (t in threads) t.join()

         Assert.assertEquals(NITERS*NTHREADS, count)
     }

    @Test
    fun multiple_threads_increment_with_explicit_jvm_lock_test() {
        var count = 0

        val NTHREADS = 2
        val NITERS   = 1000000

        val threads = ArrayList<Thread>()

        // My god, the lock creation was inside  the repeat!! :((
        val mutex = ReentrantLock()
        repeat(NTHREADS) {

            val t = thread {
                repeat(NITERS) {
                    mutex.lock()
                    try {
                        val res = count
                        count = res + 1
                    }
                    finally {
                        mutex.unlock()
                    }

                }
            }
            threads.add(t)
        }

        for (t in threads) t.join()

        Assert.assertEquals(NITERS*NTHREADS, count)
    }
}