package synchronizers.lockfree

import mu.KotlinLogging
import java.lang.Thread.sleep
import kotlin.concurrent.thread

private val logger = KotlinLogging.logger {}

private var number = 0

// try execute this program commenting  the Volatile annotation below
// we will explain this behaviour on next lectures
@Volatile
private var ready = false

fun main() {
    logger.info("Start!")

    val t = thread {
        while(!ready);
        logger.info("number: {}", number)
    }

    sleep(1000)

    number = 42
    ready = true

    t.join()
    logger.info("Done!")
}