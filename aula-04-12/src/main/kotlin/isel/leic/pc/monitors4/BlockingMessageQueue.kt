package isel.leic.pc.monitors4

import isel.leic.pc.utils.await
import isel.leic.pc.utils.dueTime
import isel.leic.pc.utils.isPast
import isel.leic.pc.utils.isZero
import java.security.InvalidParameterException
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

class BlockingMessageQueue<T>(private val capacity: Int) {

    @Throws(InterruptedException::class)
    fun tryEnqueue(messages: List<T>, timeout: Duration): Boolean {
        return false
    }

    @Throws(InterruptedException::class)
    fun tryDequeue(timeout: Duration): T? {
       return null
    }

}