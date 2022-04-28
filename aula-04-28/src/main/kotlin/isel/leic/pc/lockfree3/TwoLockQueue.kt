package isel.leic.pc.lockfree3

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class TwoLockQueue<E> {

    private class Node<E>( val value : E? = null) {
        @Volatile
        var next : Node<E>? = null
    }

    private var head : Node<E>
    private var tail : Node<E>

    constructor() {
        val dummy = Node<E>()
        head = dummy
        tail = dummy
    }

    private val putLock = ReentrantLock()
    private val takeLock = ReentrantLock()

    fun put(elem: E) {
        putLock.withLock {
            val newNode = Node<E>(elem)
            tail.next = newNode
            tail = newNode
        }
    }

    fun take() : E? {
        takeLock.withLock {
            head.next ?: return null
            // note that we change here the dummy node
            // this is crucial to avoid using head and tail at the same time!
            head = head.next!!;
            return head.value
        }
    }
}