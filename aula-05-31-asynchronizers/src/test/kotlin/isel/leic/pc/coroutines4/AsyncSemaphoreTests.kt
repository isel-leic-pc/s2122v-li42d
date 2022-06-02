package isel.leic.pc.coroutines4

import isel.leic.pc.coroutines4.asynchronizers.AsyncSemaphore
import kotlinx.coroutines.*
import org.junit.Test
import org.junit.Assert.*

import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class AsyncSemaphoreTests {

    @Test
    fun acquire_with_timeout_test() {
        runBlocking {
            val sem = AsyncSemaphore(0)
            try {
                sem.acquire(1000.toDuration(DurationUnit.MILLISECONDS))
                fail("Not supposed to be here")
            }
            catch(e: Exception) {
                println("Exception: $e")
            }
            assertEquals(0, sem.pendingAcquiresCount)
        }
    }

    @Test
    fun acquire_with_cancel_test() {
        runBlocking {
            val sem = AsyncSemaphore(0)
            var exc : CancellationException? = null
            val job = launch {
                try {
                    sem.acquire(2000.toDuration(DurationUnit.MILLISECONDS))
                }
                catch(e: CancellationException) {
                    exc=e
                    println(e)
                }
            }

            delay(1000)
            job.cancel()
            job.join()

            assertTrue(exc is java.util.concurrent.CancellationException)
        }
    }


    @Test
    fun acquire_with_success() {
        runBlocking {
            val sem = AsyncSemaphore(1)
            var res = false
            val job = launch {
                sem.acquire(2000.toDuration(DurationUnit.MILLISECONDS ))
                res= true
            }

            delay(1000)

            assertEquals(true, res)
        }
    }

    @Test
    fun `async semaphore multiple release aquires test`() {
        val sem = AsyncSemaphore(0)

        runBlocking {
            repeat(100000) {
                coroutineScope {
                    launch(Dispatchers.Default) {

                        try {
                            sem.acquire(2.toDuration(DurationUnit.SECONDS))

                        }
                        catch(e: TimeoutCancellationException) {
                            println("timeout!")
                            throw Error("timeout!")
                        }

                    }

                    sem.release()
                }
            }
        }

        println("Done")

    }

}

