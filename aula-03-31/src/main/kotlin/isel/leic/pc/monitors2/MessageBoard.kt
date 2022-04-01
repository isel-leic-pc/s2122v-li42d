package isel.leic.pc.monitors2

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

class MessageBoard<M> {
    private val monitor = ReentrantLock()
    private val msgPublished = monitor.newCondition()

    fun publish(message: M, exposureDuration: Duration) {
        monitor.withLock {

        }
    }

    @Throws(InterruptedException::class)
    fun consume(timeout: Duration) : M? {
        monitor.withLock {
            return null
        }
    }

    @Throws(InterruptedException::class)
    fun consume() : M? = consume(Duration.INFINITE)
}