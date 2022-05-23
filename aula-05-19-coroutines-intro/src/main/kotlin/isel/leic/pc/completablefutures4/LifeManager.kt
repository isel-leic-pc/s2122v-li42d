package isel.leic.pc.completablefutures4

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class LifeManager(val startAction : () -> Unit) {
    private val _counter = AtomicInteger()
    private val _inShutdown = AtomicBoolean()
    private val terminated = CompletableFuture<Boolean>()


    val inShutdown : Boolean
        get() = _inShutdown.get()

    val count : Int
        get() = _counter.get()

    fun shutdown() : CompletableFuture<Boolean> {
        if (_inShutdown.compareAndSet(false,true)) {
            startAction()
          //  if (count == 0)
          //      terminated.complete(true)
        }
        return terminated
    }

    fun enter() : Boolean {
        _counter.incrementAndGet()
        if (_inShutdown.get())  {
            leave()
            return false
        }
        return true
    }

    fun leave() : Int {
        val count = _counter.decrementAndGet()
        if (_inShutdown.get() && count == 0) {
            terminated.complete(true)
        }
        return count
    }


}