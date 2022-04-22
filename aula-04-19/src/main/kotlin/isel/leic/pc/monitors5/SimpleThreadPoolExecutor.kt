package isel.leic.pc.monitors5

import isel.leic.pc.monitors4.utils.NodeList
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.time.Duration

class SimpleThreadPoolExecutor(private val maxThreadPoolSize: Int) {

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
            }
            catch(e : Exception) {
                // here we can safely ignore the exception
            }
        }
    }

    private val monitor = ReentrantLock()
    private val pendingWorkers = NodeList<Worker>()

    // mutable state
    private var size = 0 // number of threads

    /**
     * this method contains the code run by the worker threads
     */
    private fun run(worker : Worker) {
        while(true) {
            // It is crucial that the command execution
            // by the worker is done outside the monitor lock!
            // If not, just one pool thread could execute a cmd at a given time!
            worker.safeCmdExec()
            monitor.withLock {
                // in the next lecture we will complete this code
                TODO()
            }
        }
    }

    @Throws(InterruptedException::class, RejectedExecutionException::class)
    fun execute(cmd : Runnable, timeout: Duration) : Boolean {
         monitor.withLock {
             // fast path
             if (pendingWorkers.size > 0) {
                 val worker = pendingWorkers.removeFirst().value
                 worker.cmd = cmd
                 worker.condition.signal()
                 return true
             }
             if (size < maxThreadPoolSize) {
                 Worker(monitor.newCondition(), cmd)
             }
             // wait path
             // we will complete this code in the next lecture
             TODO()
         }
    }

    fun shutdown() {
        // we will complete this code in the next lecture
        TODO()
    }

    @Throws(InterruptedException::class)
    fun awaitTermination(timeout : Duration) : Boolean {
        // we will complete this code in the next lecture
        TODO()
    }
}