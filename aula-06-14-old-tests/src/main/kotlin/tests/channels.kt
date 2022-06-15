package tests

import isel.leic.pc.kotlin_concurrency.utils.elements
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce

suspend fun channel_error() = coroutineScope {
    println("corotineScope context: ${  coroutineContext.elements()}")
    println("children:")
    println(coroutineContext.job.children)

    val channel = produce<Int> {
        println("produce context: ${  coroutineContext.elements()}")

        delay(5000);
            send(3)
            delay(1000);
            throw Error("Bad value!")

    }

    try {
        channel.consumeEach {
            println(it)
        }
    }
    catch(e: Exception) {
        println("error: $e" )
    }
}

suspend fun channel_error2() = coroutineScope {
    val channel = Channel<Int>()
    println("corotineScope context: ${  coroutineContext.elements()}")
    println("children:")
    println(coroutineContext.job.children)
    launch(Dispatchers.Default) {
        try {
            println("produce context: ${coroutineContext.elements()}")

            delay(5000);
            channel.send(3)
            delay(1000);

        }
        finally {
            channel.close()
        }

    }

    launch {
        try {
            for(i in channel) {
                println(i)
            }
        }
        catch(e: Exception) {
            println("error: $e" )
        }
    }

}
private suspend fun main()   {
    runBlocking {
        try {
            channel_error2()
        }
        catch(e: Exception) {
            println("error: ${e.message}")
        }
    }

    println("Done!")
}


