package isel.leic.pc.lockfree3

import java.util.concurrent.atomic.AtomicReference

/**
 * Treiber algorithm for a lock-free stack
 */
class StackLF<E> {

    private class Node<E>( val value : E) {
        var next : Node<E>? = null
    }

    private val head = AtomicReference<Node<E>>()

    fun push(elem : E) {
        val newNode = Node<E>(elem)
        do {
            val obsHead = head.get()
            newNode.next = obsHead
        }
        while(!head.compareAndSet(obsHead, newNode));
    }

    fun pop() : E? {
       do {
           val obsHead = head.get() ?: return null
           if (head.compareAndSet(obsHead, obsHead.next)) {
               return obsHead.value
           }
       }
       while(true)
    }

}