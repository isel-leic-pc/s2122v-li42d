package isel.leic.pc.coroutines2.cancelation

import isel.leic.pc.coroutines2.childrenStates
import isel.leic.pc.coroutines2.state
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.lang.Thread.sleep
import kotlin.math.log

private val logger= KotlinLogging.logger {}

private fun runBlocking_multiple_cancellation() {
    logger.info("Start")
    runBlocking {
        val job = launch(Dispatchers.Default) {

            launch {
                logger.info("start first child coroutine")
                delay(2000)
                logger.info("end first child coroutine")
            }

            launch {
                logger.info("start second child coroutine")
                delay(5000)
                logger.info("end second child coroutine")
            }

        }

        logger.info("before cancel coroutine")
        val children = job.children.toList()
        println(children)
        job.childrenStates()
        job.cancel()

        println("job state = ${job.state}")
        println("job children states:")
        job.childrenStates()
    }
    logger.info("End")
}

private fun non_suspending_coroutine_cancellation() {
    runBlocking {
        val job = launch(Dispatchers.Default) {
            while(coroutineContext[Job]?.isActive == true) {
                sleep(1000)
                logger.info("one more step")
            }
        }

        job.cancel()
        job.join()
    }
}

private fun main() {
    non_suspending_coroutine_cancellation()
}