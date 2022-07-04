package isel.leic.pc

import isel.leic.pc.utils.await
import isel.leic.pc.utils.dueTime
import isel.leic.pc.utils.isPast
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration


/**
 * Wrong implementation with monitor classic style
 */
class BadExchanger<T> {
    val monitor = ReentrantLock()
    val cond = monitor.newCondition()

    private class Holder<T>(var value : T? = null)

    // in classic monitor style we have a monitor state
    // shared by all clients
    private var current = Holder<T>()

    fun exchange(value : T, timeout: Duration) : T? {
        monitor.withLock {
            // fast path
            // note that we can be a third thread
            // entering the monitor before the awoken partner (barging)
            // this cause a fail in the Exchange behaviour!
            if (current.value != null) {
                val result = current.value
                current.value = value
                cond.signal()
                return result
            }
            if (timeout == Duration.ZERO) return null

            // wait path
            current.value = value
            val dueTime = timeout.dueTime()
            do {
                try {
                    cond.await(dueTime)
                    // in classic style we recheck global state after
                    // wait in order to evaluate if we can proceed
                    // due to barging, it's possible that the value
                    // in current comes from a third client trying
                    // to an exchange. Not good!
                    if (current.value  != value ) {
                        val result = current.value
                        current.value = null
                        return result
                    }

                    if (dueTime.isPast) {
                        current.value = null
                        return null
                    }
                }
                catch(e: InterruptedException) {
                    if (current.value != value) {
                        Thread.currentThread().interrupt()
                        val result = current.value
                        current.value = null
                        return result
                    }
                    current.value = null
                    throw e
                }
            }
            while(true)
        }
    }
}

/**
 * Correct series 1 Exchanger
 * this monitor must be implemented with kernel style in order
 * to support specified semantic
 */
class Exchanger<T> {
    val monitor = ReentrantLock()
    val cond = monitor.newCondition()

    private class Holder<T>(val first : T) {
        var second : T? = null
    }

    private var current : Holder<T>? = null

    fun exchange(value : T, timeout: Duration) : T? {
        monitor.withLock {
            // fast path
            if (current != null) {
                // kernel style oblige

                current?.second = value     // the value is sent to partner
                cond.signal()               // the partner is awoken
                val res = current?.first

                current = null              // shared state associated with it is removed

                return res
            }
            if (timeout == Duration.ZERO) return null

            // wait path

            // note that the holder is local to first partner
            val holder = Holder(value)

            // copy to monitor shared state.
            // This is similar to add to a node to an waiting list,
            // in scenarios where several waiters are possible
            current = holder
            val dueTime = timeout.dueTime()
            do {
                try {
                    cond.await(dueTime)
                    // with kernel style
                    // the notified NEVER touch
                    // monitor shared state on monitor reentry
                    if (holder.second != null)
                        return holder.second
                    if (dueTime.isPast) {
                        current = null
                        return null
                    }
                }
                catch(e: InterruptedException) {
                    if (holder.second != null) {
                        Thread.currentThread().interrupt()
                        return holder.second
                    }
                    current = null
                    throw e
                }
            }
            while(true)
        }
    }
}