package isel.leic.pc.coroutines2.cancelation


import isel.leic.pc.coroutines2.childrenStates
import isel.leic.pc.coroutines2.state
import kotlinx.coroutines.*
import mu.KotlinLogging

private val logger= KotlinLogging.logger {}

private suspend fun simpleGlobalCancel() {

    val job = GlobalScope.launch {
        logger.info("start coroutine")
        delay(2000)
        logger.info("end coroutine")
    }

    delay(1000)
    logger.info("cancel coroutine")
    job.cancel()

    println("job state = ${job.state}")
}

private suspend fun multipleGlobalCancel() {

    val job = GlobalScope.launch {

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

    delay(3000)
    logger.info("cancel coroutine")
    job.cancel()

    println("job state = ${job.state}")
    println("job children states:")
    job.childrenStates()
}

private fun main() {
    logger.info("Start")
    runBlocking {
        logger.info("Start runBlocking")
        multipleGlobalCancel()
        logger.info("End runBlocking")
    }

    logger.info("End")
}