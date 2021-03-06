package isel.leic.pc.coroutines4.asynchronizers

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.resume
import kotlin.time.Duration

private val logger= KotlinLogging.logger {}

class AsyncSemaphore(initialPermits: Int) {

    private class PendingAcquire(val cont: CancellableContinuation<Unit>) {
        var completed = false
    }

    private var permits = initialPermits
    private val monitor = ReentrantLock()
    private val pendingAcquires = mutableListOf<PendingAcquire>()

    private fun internalAcquire(cont: CancellableContinuation<Unit>) : PendingAcquire? {

        monitor.withLock {
            if (permits > 0) {
                permits--
                return null
            }

            val node = PendingAcquire(cont)
            pendingAcquires.add(node)
            return node
        }
    }

    // on cancellation/timeout remove the node if it was still in then
    // pending list
    private fun cancelCleanup(node : PendingAcquire?) {
        if (node != null) {
            monitor.withLock {
                if (!node.completed) {
                    pendingAcquires.remove(node)
                    node.completed = true
                }
            }
        }
    }

    suspend fun acquire(timeout : Duration)  {
        // fast path
        monitor.withLock {
            if (permits > 0) {
                permits--
                return
            }
        }

        withTimeout(timeout) {
            suspendCancellableCoroutine<Unit> { cont ->

                val node = internalAcquire(cont)

                if (node != null) {
                    // regist callback for cleanup
                    // on cancel/timeout
                    cont.invokeOnCancellation {
                        cancelCleanup(node)
                    }
                }
                else {
                    cont.resume(Unit)
                }
            }
        }

    }

    fun release() {
        var node : PendingAcquire? = null
        monitor.withLock {
           if (pendingAcquires.isEmpty())
               permits++
           else {
               node = pendingAcquires.removeFirst()
               node?.completed = true
           }
        }
        node?.cont?.resume(Unit)
    }

    // Just for debug purposes
    val pendingAcquiresCount : Int
        get() {
            monitor.withLock {
                return pendingAcquires.size
            }
        }
}