package isel.leic.pc.monitors3

import isel.leic.pc.utils.await
import isel.leic.pc.utils.dueTime
import isel.leic.pc.utils.isPast
import isel.leic.pc.utils.toDuration
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

class SimpleFuture<T>(val supplier : Callable<T>) : Future<T> {

    enum class State { NEW, ACTIVE, DONE, CANCELLED }

    val monitor = ReentrantLock()
    val hasValue = monitor.newCondition()

    // mutable state
    var state = State.NEW
    var value : T? = null
    var exception: Exception? = null
    lateinit  var thread : Thread

    /**
     * Start the value production in another thread
     * created on purpose
     */
    fun start() {
        monitor.withLock {
            if (state != State.NEW)
                throw IllegalStateException()
            state = State.ACTIVE

        }
        thread = thread {
            run()
        }
    }

    /**
     *
     * Produce the value and collect result or exception
     */
    private fun run() {
        try {
            val result = supplier.call()
            trySetState(result, null)
        }
        catch(e : Exception) {
            trySetState(null, e)
        }
    }

    /**
     * try produce state and result/error
     */
    private fun trySetState(result : T?, error : Exception?) : Boolean {
        monitor.withLock {
            if (state != State.ACTIVE) return false
            if (result != null) {
                state = State.DONE
                value = result
            }
            else {
                state= State.CANCELLED
                exception = error
            }
            return true;
        }

    }

    /**
     * cancel future operation
     */
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        // change state to CANCELLED
        if (mayInterruptIfRunning)
            thread.interrupt()

        return trySetState(null, CancellationException())
    }

    override fun isCancelled(): Boolean {
        monitor.withLock {
            return state == State.CANCELLED
        }
    }

    override fun isDone(): Boolean {
        monitor.withLock {
            return state == State.DONE
        }
    }

    override fun get(): T {
        return get(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
    }

    override fun get(timeout: Long, unit: TimeUnit): T {
        monitor.withLock {
            // fast path
            if (state == State.DONE) {
                return value!!
            }
            if (state == State.CANCELLED)
                throw exception!!
            if (timeout == 0L) throw TimeoutException()
            //wait path

            // convert timeout from Java TimeUnit to kotlin Duration
            val duration = timeout.toDuration(unit)
            val dueTime = duration.dueTime()

            do {
                hasValue.await(dueTime)
                if (state == State.DONE) return value!!
                if (state == State.CANCELLED) throw CancellationException()
                if (dueTime.isPast) throw TimeoutException()
            }
            while(true)
        }
    }

}