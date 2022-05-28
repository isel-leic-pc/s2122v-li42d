package isel.leic.pc.kotlin_concurrency.suspendables

import kotlinx.coroutines.*
import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

val logger = KotlinLogging.logger {}

private val executor =
    Executors.newSingleThreadScheduledExecutor()

private fun stopExecutor() {
    executor.shutdown()

}

suspend fun myDelay(delay: Long)  {
    suspendCancellableCoroutine<Unit>   { cont ->
        val job = executor.schedule({
            logger.info("delay terminated")
            if (cont.isActive) {
                cont.resume(Unit)
            }

        }, delay, TimeUnit.MILLISECONDS)


        cont.invokeOnCancellation {
            println("cancelled!")
            job.cancel(true)
        }


    }
}

private fun main() {
    runBlocking {
        val job = launch(Dispatchers.IO) {
            println("enter myDelay")
            try {
                myDelay(10000)
            }
            catch(e: Exception) {
                println("an exception ocurred in delay: $e")
            }
        }

        delay(1000)
        job.cancel()

        println("after cancel")
        delay(2000)
        job.join()
        println("test done!")
        stopExecutor()
    }
}

