package isel.leic.pc.monitors3

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

    class MsgWrapper<M>(var msg : M? = null);

    class PubMessage<M>(val msg : M?, exposureDuration: Duration) {
        val dueTime = exposureDuration.dueTime()

        val isValid : Boolean
            get() { return !dueTime.isPast }
    }

    val pendingConsumers = mutableListOf<MsgWrapper<M>>()
    val EMPTY_MSG = PubMessage<M>(null, 0.toDuration(DurationUnit.MILLISECONDS))
    var pub =  EMPTY_MSG

    private fun notifyWaiters(pubMsg : M) {
        /*
        while(pendingConsumers.size > 0) {
            val waiter = pendingConsumers.removeAt(0)
            waiter.msg = pubMsg
        }
        */

        for( w in pendingConsumers) {
            w.msg = pubMsg
        }
        pendingConsumers.clear()
        msgPublished.signalAll()
    }

    fun publish(message: M, exposureDuration: Duration) {
        monitor.withLock {
            if (exposureDuration.isZero)
                pub = EMPTY_MSG
            else
                pub = PubMessage<M>(message, exposureDuration)
            notifyWaiters(message)

        }
    }

    @Throws(InterruptedException::class)
    fun consume(timeout: Duration = Duration.INFINITE) : M? {
        monitor.withLock {
             // fast path
            if (pub.isValid) return pub.msg
            if (timeout.isZero) return null

            //wait path

            val dueTime = timeout.dueTime()
            val waiter = MsgWrapper<M>()
            pendingConsumers.add(waiter)
            do {
                try {
                    msgPublished.await(dueTime)
                    if (waiter.msg != null) return waiter.msg
                    if (dueTime.isPast) {
                        pendingConsumers.remove(waiter)
                        return null
                    }
                }
                catch(e: InterruptedException) {
                    if (waiter.msg != null) {
                        Thread.currentThread().interrupt()
                        return waiter.msg
                    }
                    pendingConsumers.remove(waiter)
                    throw e
                }
            } while(true)

        }
    }

}