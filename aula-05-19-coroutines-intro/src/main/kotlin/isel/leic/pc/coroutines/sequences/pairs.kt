package isel.leic.pc.coroutines.sequences

import mu.KotlinLogging

val logger = KotlinLogging.logger {}



fun <T> sequence( block: suspend  MySequenceScope<T>.() -> Unit) : MySequenceScope<T> {
    return MySequenceScope(block)
}







fun main() {

    val pairs : Sequence<Int> = sequence {
        logger.info("Start")
        var curr = 2

        while (true) {
            yield(curr) // next Fibonacci number
            curr += 2
        }
    }

    pairs
    .take(20)
    .forEach { logger.info("$it") }
}