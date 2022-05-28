package isel.leic.pc.asynchronizers

import isel.leic.pc.coroutines3.asynchronizers.AsyncSemaphore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class AsynchronizersTests {

    @Test
    fun acquire_with_timeout_test() {
        runBlocking {
            val sem = AsyncSemaphore(0)

            sem.acquire(1000.toDuration(DurationUnit.MILLISECONDS))

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
                res = sem.acquire(2000.toDuration(DurationUnit.MILLISECONDS))
            }

            delay(1000)

            assertEquals(true, res)
        }
    }
}