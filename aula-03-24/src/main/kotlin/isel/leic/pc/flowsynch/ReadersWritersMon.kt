package isel.leic.pc.flowsynch

import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ReadersWritersMon {
    private var nReaders = 0
    private var waitingReaders = 0
    private val mutex = ReentrantLock()
    private var writing = false
    private val canRead = mutex.newCondition()
    private val canWrite = mutex.newCondition()


    fun enterReader() {
        mutex.withLock {
           waitingReaders++
           while(writing) {
               canRead.await()
           }
           waitingReaders--
           nReaders++
        }
    }

    fun leaveReader() {
        mutex.withLock {
            nReaders--
            if (nReaders == 0)
                canWrite.signal()
        }
    }

    fun enterWriter() {
        mutex.withLock {
            while(nReaders > 0 || writing) {
                canWrite.await()
            }
            writing = true
        }
    }

    fun leaveWriter() {
        mutex.withLock {
            writing = false
            if (waitingReaders > 0)
                canRead.signalAll()
            else
                canWrite.signal()
        }
    }
}