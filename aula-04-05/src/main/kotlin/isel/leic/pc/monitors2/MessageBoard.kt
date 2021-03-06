package isel.leic.pc.monitors2

import isel.leic.pc.utils.await
import isel.leic.pc.utils.dueTime
import isel.leic.pc.utils.isPast
import isel.leic.pc.utils.isZero
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MessageBoard<M> {
    private val monitor = ReentrantLock()
    private val msgPublished = monitor.newCondition()


    class PubMessage<M>(val msg : M?, exposureDuration: Duration) {
        val dueTime = exposureDuration.dueTime()

        val isValid : Boolean
            get() { return !dueTime.isPast }
    }

    var pub =  PubMessage<M>(null, 0.toDuration(DurationUnit.MILLISECONDS))

    fun publish(message: M, exposureDuration: Duration) {
        monitor.withLock {
            TODO()
        }
    }

    @Throws(InterruptedException::class)
    fun consume(timeout: Duration = Duration.INFINITE) : M? {
        monitor.withLock {
             // fast path
            if (pub.isValid) return pub.msg
            TODO()
        }
    }

}