package isel.leic.pc.completablefutures.errorhandling

import mu.KotlinLogging
import java.lang.Thread.sleep
import java.util.concurrent.CompletableFuture

private val logger = KotlinLogging.logger {}

fun oper1Async() : CompletableFuture<Int> {
    return CompletableFuture.supplyAsync {
        sleep(2000)
        logger.info("produce oper1Async result")
        2
    }
}

fun oper2Async(id : Int) : CompletableFuture<String> {

    return CompletableFuture.supplyAsync {
        try {
            sleep(4000)
            //logger.info("throw error on oper2Async")
            throw IllegalStateException("Error Simulation")
            logger.info("produce oper2Async result")
            "hello"
        }
        catch(e: Exception) {
            logger.info("throw error $e oper2Async")
            throw e
        }
    }
}

fun oper3Async(name : String) : CompletableFuture<Int> {
    return CompletableFuture.supplyAsync {
        sleep(8000)
        logger.info("produce oper3Async result")
        3
    }
}

private fun main() {
    /*
    val fut =   oper1Async()
                .thenCompose {
                    oper2Async(it)
                }
                .thenCompose {
                    oper3Async(it)
                }
                .exceptionally {
                   -1
                }
                .thenAccept {
                    logger.info("final result = $it")
                }

    sleep(10000)
    logger.info("done first")
    */

    val cf1 = oper1Async()
    val cf2 = oper2Async(3)
    val cf3 = oper3Async("teste")

    CompletableFuture.allOf(
        cf1,
        cf2,
        cf3
    )
    .whenComplete {
        v, e ->
            if (e != null) {
                logger.info("final error: $e")

                logger.info("cf1 state = ${cf1.isCancelled || cf1.isCompletedExceptionally}")
                logger.info("cf2 state = ${cf2.isCancelled || cf2.isCompletedExceptionally}")
                logger.info("cf3 state = ${cf3.isCancelled || cf3.isCompletedExceptionally}")
            }
            else
                logger.info("final result = ${cf3.join()}")
    }

    logger.info("cancel cf1")
    //cf3.cancel(true)
    sleep(10000)
    logger.info("done all")
}