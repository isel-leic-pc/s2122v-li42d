package isel.leic.pc

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.*


/**
 * from first test of 2021v
 */
class BroadcastBox<T> {

    private var waiters = mutableListOf<Continuation<T>>()
    private val lock = ReentrantLock()

    suspend fun waitForMessage() : T {
        return suspendCoroutine { cont ->
            lock.withLock {
                waiters.add(cont)
            }
        }
    }

    fun sentToAll(msg: T): Int {
        var waitersCopy : List<Continuation<T>>? = null
        lock.withLock {
            if (waiters.isEmpty()) return 0
            waitersCopy = waiters
            waiters = mutableListOf()
        }
        waitersCopy!!.forEach { cont->
            cont.resume(msg)
        }
        return waitersCopy!!.size
    }
}