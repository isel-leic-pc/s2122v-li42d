package isel.leic.pc.coroutines.basics

import isel.leic.pc.coroutines.utils.endTimers
import isel.leic.pc.coroutines.utils.list
import isel.leic.pc.coroutines.utils.myDelay
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.lang.Thread.sleep
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val logger = KotlinLogging.logger {}


/**
 * launch a single coroutine in global scope
 */
private fun first_coroutine_with_globalScope_builder() {

    GlobalScope.launch {
        logger.info("Hello, ")
        // a blocking function in a coroutine... ugh!...
        sleep(2000)
        logger.info(", World!")
    }
    logger.info("end function")

}


/**
 * launch multiple corputines independently in GlobalScope
 */
private fun multiple_coroutines_with_globalScope_builder() {
    GlobalScope.launch {
        logger.info("coroutine start")
        delay(1000)
        println("Hello1")
        logger.info("coroutine end")
    }

    GlobalScope.launch {
        logger.info("coroutine start")
        delay(1000)
        println("Hello2")
        logger.info("coroutine end")
    }

    GlobalScope.launch {
        logger.info("coroutine start")
        delay(1000)
        println("Hello3")
        logger.info("coroutine end")
    }

    // naif form of blocking for coroutines termination
    sleep(2000)
    println("Done")

}

/**
 * runBlocking is a special builder that blocks the current thread
 * until all the launched coroutines terminate
 */
private fun multiple_coroutines_with_runBloking_builder() {
    runBlocking {
        launch(CoroutineName("first_coroutine") +
                  Dispatchers.Default) {
            println(coroutineContext.list())
            logger.info("Hello1, ")
            myDelay(3000)
            logger.info(", World1!")
        }
        logger.info("after first launch")
        launch(CoroutineName("jose")) {
            println(coroutineContext.list())
            logger.info("Hello2, ")
            myDelay(5000)
            logger.info(", World2!")
        }
        logger.info("after second launch")
    }

    println("Done")

    // to terminate myDelay support executor
    endTimers()
}

private fun main() {
    logger.info("app start")
    multiple_coroutines_with_runBloking_builder()

    logger.info("app end")

}