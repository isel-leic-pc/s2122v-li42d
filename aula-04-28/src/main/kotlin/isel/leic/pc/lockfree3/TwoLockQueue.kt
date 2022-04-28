package isel.leic.pc.lockfree3

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class TwoLockQueue<E> {

    private class Node<E>( val value : E? = null) {
         var next : Node<E>? = null
    }

    private val putLock = ReentrantLock()
    private val takeLock = ReentrantLock()

    fun offer(elem: E) {
        putLock.withLock {
            TODO()
        }
    }

    fun poll() : E? {
        takeLock.withLock {
           TODO()
        }
    }
}