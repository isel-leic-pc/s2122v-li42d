package isel.leic.pc.lockfree3

import java.util.concurrent.atomic.AtomicReference

class QueueLF<T> {

    private class Node<T> (   val value : T? = null) {
        val next = AtomicReference<Node<T>?>()
    }


    fun enqueue(elem : T) {
        TODO()
    }

    fun dequeue() : T? {
       TODO()
    }

}