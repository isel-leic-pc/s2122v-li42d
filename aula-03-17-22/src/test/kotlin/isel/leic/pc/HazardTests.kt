package isel.leic.pc

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

class HazardTests {
    private val NTRANSFERS = 1_000_000

    private fun transfers(accounts: Array<Account>, idx: Int) {
        val dstIdx = (1 + idx) % 2
        val src = accounts[idx]
        val dst = accounts[dstIdx]
        for (i in 0 until  NTRANSFERS) {

            //System.out.printf("start transfer from %d to %d\n", srcIdx, dstIdx);
            src.transfer2(dst, 10)
            //System.out.printf("end transfer from %d to %d\n", srcIdx, dstIdx);
        }
    }

     @Test
     fun multiple_threads_increment_test() {
         var count = 0

         val NTHREADS = 4
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

         assertEquals(NITERS*NTHREADS, count)
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

        assertEquals(NITERS*NTHREADS, count)
    }

    @Test
    fun multiple_account_transfer_test() {
        val accounts : Array<Account> = arrayOf(
            Account(1000),
            Account(1000)
        )

        val threads =
            IntRange(0,1)
                .map {
                    thread {
                        transfers(accounts, it)
                    }
                }

        threads.forEach {
            it.join()
        }
        assertEquals(2000, accounts[0].getBalance() + accounts[1].getBalance())
    }
}