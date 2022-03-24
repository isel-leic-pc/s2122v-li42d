package isel.leic.pc.flowsynch

import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Queue<T>(val capacity : Int) {
    val elems = LinkedList<T>()
    val avaiableElems = Semaphore(0)
    val avaiableSpace = Semaphore(capacity)
    val mutex = ReentrantLock()

    fun get() : T? {
        avaiableElems.acquire()
        var elem : T
        mutex.withLock {
           elem  =  elems.removeFirst()
        }
        avaiableSpace.release()
        return elem
    }

    fun put(elem : T) {
        avaiableSpace.acquire()
        mutex.withLock {
            elems.add(elem)
        }
        avaiableElems.release()
    }
}