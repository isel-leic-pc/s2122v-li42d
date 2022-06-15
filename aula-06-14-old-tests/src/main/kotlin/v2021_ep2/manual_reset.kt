package v2021_ep2

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.List.copyOf
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.resume

class ManualResetEvent {
    var signaled = false
    var lock = ReentrantLock()

    class Waiter(val cont: CancellableContinuation<Unit> );

    private val waiters = mutableListOf<Waiter>()

    suspend fun waitAsync() {
        suspendCancellableCoroutine<Unit> { cont ->
            var waiter : Waiter? = null
            lock.withLock {
               if (!signaled) {
                   waiter = Waiter(cont)
                   waiters.add(waiter!!)
               }
            }
            if (waiter == null) cont.resume(Unit)
        }
    }

    fun set() {
        var waitersCopy  : List<Waiter>? = null
        lock.withLock {
            if (waiters.size > 0)
                waitersCopy = copyOf(waiters)
            waiters.clear()
            signaled = true
        }

        waitersCopy?.forEach {
            it.cont.resume(Unit)
        }
    }

    fun clear() {
        lock.withLock {
            signaled = false
        }
    }
}