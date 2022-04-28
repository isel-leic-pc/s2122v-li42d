package isel.leic.pc.monitors6

import isel.leic.pc.monitors4.utils.NodeList
import isel.leic.pc.utils.await
import isel.leic.pc.utils.dueTime
import isel.leic.pc.utils.isPast
import isel.leic.pc.utils.isZero
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.time.Duration

class SimpleThreadPoolExecutor(private val maxThreadPoolSize: Int) {

    enum class State { ACTIVE, SHUTDOWN_STARTED, TERMINATED }

    private class WorkItem(val cmd : Runnable, val delivered : Condition) {
        var done = false
    }

    /**
     * This class represents a thread pool worker.
     * It's an inner class in order to give direct access to the Executor instance
     */
    private inner class Worker(val condition : Condition, var cmd : Runnable? = null) {
        var thread : Thread? = null

        /**
         * Starts a new thread to execute the worker code
         */
        fun start() {
            thread = thread {
                run(this)
            }
        }

        /**
         * Is used to run the cmd ignoring eventual exceptions
         * thrown by the external code
         */
        fun safeCmdExec() {
            try {
                cmd?.run()
                cmd = null
            }
            catch(e : Exception) {
                // here we can safely ignore the exception
            }
        }

        /**
         * Terminate worker thread due to shut down.
         * Is supposed to be called by the lock owner
         */
        fun terminate() {
            if (--size == 0 && state == State.SHUTDOWN_STARTED) {
                state = State.TERMINATED
                terminatedPool.signalAll()
            }
        }
    }

    private val monitor = ReentrantLock()
    private val pendingWorkers = NodeList<Worker>()
    private val pendingItems = NodeList<WorkItem>()
    private val terminatedPool = monitor.newCondition()

    // mutable state
    private var size = 0 // number of threads
    private var state = State.ACTIVE

    /**
     * this private method contains the code run by the worker threads
     */
    private fun run(worker : Worker) {
        while(true) {
            // It is crucial that the command execution
            // by the worker is done outside the monitor lock!
            // If not, just one pool thread could execute a cmd at a given time!
            worker.safeCmdExec()
            monitor.withLock {
                // fast path
                if (pendingItems.size > 0) {
                    val item = pendingItems.removeFirst().value
                    item.done = true
                    item.delivered.signal()
                    worker.cmd = item.cmd
                }
                else {
                    val node = pendingWorkers.add(worker)
                    do {
                        worker.condition.await()
                        if (state != State.ACTIVE) return
                    }
                    while(worker.cmd == null)
                }
            }

        }
    }

    @Throws(InterruptedException::class, RejectedExecutionException::class)
    fun execute(timeout: Duration, cmd : Runnable) : Boolean {

         monitor.withLock {
             if (state != State.ACTIVE)
                 throw RejectedExecutionException()
             // fast path
             if (pendingWorkers.size > 0) {
                 val worker = pendingWorkers.removeFirst().value
                 worker.cmd = cmd
                 worker.condition.signal()
                 return true
             }
             if (size < maxThreadPoolSize) {
                 size++
                 Worker(monitor.newCondition(), cmd).start()
             }
             // wait path
             if (timeout.isZero) return false
             val dueTime = timeout.dueTime()
             val node = pendingItems.add(WorkItem(cmd, monitor.newCondition()))
             do {
                 try {
                     node.value.delivered.await(dueTime)
                     if (node.value.done) return true
                     if (dueTime.isPast) {
                         pendingItems.remove(node)
                         return false
                     }
                 }
                 catch(e: InterruptedException) {
                     if (node.value.done) {
                         Thread.currentThread().interrupt()
                         return true
                     }
                     pendingItems.remove(node)
                     throw e
                 }
             }
             while(true)
         }
    }

    private fun notifyWorkers() {
        for (worker in pendingWorkers) {
            worker.condition.signal()
        }
        pendingWorkers.clear()
    }

    fun shutdown() {
        monitor.withLock {
            if (state !=  State.ACTIVE) return
            state = State.SHUTDOWN_STARTED
            notifyWorkers()
        }
    }

    @Throws(InterruptedException::class)
    fun awaitTermination(timeout : Duration) : Boolean {
        monitor.withLock {
            // fast path
            if (state == State.TERMINATED) return true
            if (timeout.isZero) return false
            val dueTime = timeout.dueTime()
            do {
                terminatedPool.await(dueTime)
                if (dueTime.isPast) return false
                if (state == State.TERMINATED) return true
            }
            while(true)
        }
    }
}