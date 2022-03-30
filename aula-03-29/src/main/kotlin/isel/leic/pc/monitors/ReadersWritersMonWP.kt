package isel.leic.pc.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * With simple changes made to the notification logic
 */
class ReadersWritersMonWP {
    private var nReaders = 0
    private var waitingReaders = 0
    private var waitingWriters = 0
    private val mutex = ReentrantLock()
    private var writing = false
    private val canRead = mutex.newCondition()
    private val canWrite = mutex.newCondition()


    fun enterReader() {
        mutex.withLock {
            waitingReaders++
            while(writing || waitingWriters > 0) {
                canRead.await()
            }
            waitingReaders--
            nReaders++
        }
    }

    fun leaveReader() {
        mutex.withLock {
            nReaders--
            if (nReaders == 0 && waitingWriters > 0)
                canWrite.signal()
        }
    }

    fun enterWriter() {
        mutex.withLock {
            while(nReaders > 0 || writing || waitingWriters > 0) {
                canWrite.await()
            }
            writing = true
        }
    }

    fun leaveWriter() {
        mutex.withLock {
            writing = false
            if (waitingWriters > 0)
                canWrite.signal()
            else if (waitingReaders > 0)
                canRead.signalAll()
        }
    }
}