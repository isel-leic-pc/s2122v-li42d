package isel.leic.pc.monitors6

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.time.Duration

class SimpleThreadPoolExecutorTests {
    @Test
    fun `simple thread pool submission`() {
        val pool = SimpleThreadPoolExecutor(1)
        // A countdown latch is a synchronizer useful
        // to await for the completion of a number of worker threads
        // the number is passed on the constructor
        val cdl = CountDownLatch(1)

        val expected = 3
        var result = 0

        pool.execute(Duration.ZERO) {
            result = expected
            cdl.countDown()
        }
        cdl.await(2000, TimeUnit.MILLISECONDS)

        assertEquals(expected, result)
    }
}