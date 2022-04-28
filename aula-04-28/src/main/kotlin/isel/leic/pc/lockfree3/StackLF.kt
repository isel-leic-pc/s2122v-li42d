package isel.leic.pc.lockfree3

import java.util.concurrent.atomic.AtomicReference

class StackLF<E> {

    private class Node<E>( val value : E) {
        var next : Node<E>? = null
    }


    fun push(elem : E) {
        TODO()
    }

    fun pop() : E? {
       TODO()
    }

}