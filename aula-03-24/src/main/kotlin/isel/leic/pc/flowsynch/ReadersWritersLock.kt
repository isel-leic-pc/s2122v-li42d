package isel.leic.pc.flowsynch

import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ReadersWritersLock {
    private var nReaders = 0
    private val dataAccess = Semaphore(1)
    private val mutex = ReentrantLock()

    fun enterReader() {
        mutex.withLock {
            ++nReaders
            if (nReaders == 1)
                dataAccess.acquire()
        }
    }

    fun leaveReader() {
        mutex.withLock {
            --nReaders
            if (nReaders == 0)
                dataAccess.release()
        }
    }

    fun enterWriter() {
        dataAccess.acquire()
    }

    fun leaveWriter() {
        dataAccess.release()
    }
}