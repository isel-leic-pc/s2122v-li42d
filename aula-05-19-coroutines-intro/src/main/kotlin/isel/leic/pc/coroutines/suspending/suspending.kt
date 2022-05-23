package isel.leic.pc.coroutines.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

val logger = KotlinLogging.logger {}

private val executor =
    Executors.newSingleThreadScheduledExecutor()


fun endTimers() {
    executor.shutdown()
}


suspend fun  no_return() : Int {
    logger.info("Enter suspending function")
    return suspendCoroutine<Int> {
        // no return ever
    }
}

suspend fun myDelay(delay: Long)  {
    suspendCoroutine<Unit> {
            cont ->
        executor.schedule({
            logger.info("delay terminated")
            cont.resume(Unit)

        }, delay, TimeUnit.MILLISECONDS)
    }
}


private suspend fun main() {

    println("start")
    myDelay(100)
    println("end")

}