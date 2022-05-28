package isel.leic.pc.coroutines3.asynchronizers

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration
import kotlin.coroutines.*

private val logger= KotlinLogging.logger {}
private val schedulledPool = Executors.newScheduledThreadPool(4);
class AsyncSemaphore(initialPermits: Int) {

    private class PendingAcquire(
        val cont: CancellableContinuation<Boolean>,
        val asem : AsyncSemaphore) {
        var done = false
        var timer : ScheduledFuture<*>? = null

        val timerCallback = Runnable {
            asem.doTimeout(this)
        }

        fun start( timeout : Duration) {
            /*
            if (timeout != Duration.INFINITE) {
                timer = schedulledPool.schedule(
                    timerCallback,
                    timeout.inWholeMilliseconds,
                    TimeUnit.MILLISECONDS
                )
            }

             */
            cont.invokeOnCancellation {
                asem.doCancel(this)
            }
        }

        fun dispose() {
            timer?.cancel(true)
        }


    }
    private var permits : Int = initialPermits
    private val monitor = ReentrantLock()
    private val pendingAcquires = mutableListOf<PendingAcquire>()

    private fun doTimeout(node:PendingAcquire) {
        var doTimeout = false
        monitor.withLock {
            if (!node.done) {
                doTimeout = true
                node.done = true

            }
        }
        if  (doTimeout)
            node.cont.resume(false)
    }

    private fun doCancel(node:PendingAcquire) {
        var doCancel = false
        monitor.withLock {
            if (!node.done) {
                doCancel = true
                node.done = true
            }
        }
        if  (doCancel)
            node.dispose()
    }

    private fun internalAcquire(cont : CancellableContinuation<Boolean>,
                                timeout: Duration = Duration.INFINITE) {
        val node = PendingAcquire(cont, this)

        monitor.withLock {
            pendingAcquires.add(node)

        }
        node.start(timeout)

    }

    suspend fun acquire(timeout: Duration) : Boolean {
        monitor.withLock {
            if (permits > 0) {
                --permits
                return true
            }

        }
        val res = withTimeout(timeout) {
            suspendCancellableCoroutine<Boolean> {
                logger.info("Saving Acquire")
                internalAcquire(it, timeout)
            }
        }
        logger.info("Return from acquire")
        return res
    }

    fun release() {
        var toComplete : PendingAcquire? = null
        monitor.withLock {
            if (pendingAcquires.size > 0) {
                logger.info("Do release resuming  pending")
                val node = pendingAcquires.removeFirst()
                node.done = true
                toComplete = node
            }
            else {
                logger.info("Do release increment permits")
                permits++
            }
        }
        toComplete?.cont?.resume(true)
        toComplete?.dispose()
    }
}