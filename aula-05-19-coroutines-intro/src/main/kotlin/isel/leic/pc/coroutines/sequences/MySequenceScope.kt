package isel.leic.pc.coroutines.sequences

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.*

// Place definition above class declaration to make field static


class MySequenceScope<T>(val block : suspend MySequenceScope<T>.() -> Unit) :
    Sequence<T>, Iterable<T>, Iterator<T>, CoroutineScope {
    var done = false
    var cont  = block.createCoroutine(this, Continuation(EmptyCoroutineContext, {}))
    var nextValue : T? = null


    override fun iterator(): Iterator<T> {
        logger.info("Start sequence")
        return this;
    }

    override fun hasNext(): Boolean {
        logger.info("hasNext")
        if (done) return false
        if (nextValue != null) return true
        logger.info("resume sequence")
        cont.resume(Unit)

        return nextValue != null
    }

    override fun next(): T {
        logger.info("next")
        if (nextValue == null) throw IllegalStateException()
        val value = nextValue
        nextValue = null
        return value!!
    }

    suspend fun yield(value: T)  {
        nextValue = value
        logger.info("suspend sequence")
        val res = suspendCoroutine<Unit> {  continuation ->
            cont = continuation
        }
        logger.info("continue sequence")
        return res
    }

    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext


}