package isel.leic.pc.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.util.concurrent.CountDownLatch
import kotlin.random.Random

private val logger= KotlinLogging.logger {}

private fun main0() {
    val cdl = CountDownLatch(50_000)
    repeat(50_000) {
        val t = Thread {
            val random = Random.Default
            var sum = 0L
            logger.info("Start thread")
            repeat(10) {
                Thread.sleep(500)
                sum += random.nextInt()
            }
            logger.info("End thread with count $sum")
            cdl.countDown()
        }
        t.start()
    }
    cdl.await()
    println("done all")
}

// try different dispatchers
private fun main()  {
    runBlocking {
        repeat(50_000) {
            launch(Dispatchers.Default)  {
                val random = Random
                var sum = 0L
                logger.info("Start coroutine")
                repeat(10) {
                    delay(500)
                    sum += random.nextInt()
                }
                logger.info("End coroutine")
            }
        }
    }

    println("done all")
}