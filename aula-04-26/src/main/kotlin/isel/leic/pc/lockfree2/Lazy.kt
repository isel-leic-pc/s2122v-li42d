package isel.leic.pc.lockfree2

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * A double-checked locking version of
 * the Lazy class
 * Note that the value property is marked as @Volatile
 * This is necessary to achieve happens-before relationship
 * between the value read and access the value referenced object properties
 */
class Lazy<T>(private val factory : () -> T ) {
    @Volatile
    var value : T? = null

    val mutex = ReentrantLock()

    fun get() : T {
        if (value == null) {
            mutex.withLock {
                if (value == null)
                    value = factory()

            }
        }
        return value!!
    }
}
