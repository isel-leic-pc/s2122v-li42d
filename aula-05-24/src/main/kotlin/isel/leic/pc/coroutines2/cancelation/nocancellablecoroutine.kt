package isel.leic.pc.coroutines2.cancelation

import isel.leic.pc.coroutines2.state
import kotlinx.coroutines.*
import java.lang.Thread.sleep

fun nonCancellable() {
    val job = GlobalScope.launch {
        while(isActive) {
            println("tick")
            sleep(1000)
        }
    }

    job.cancel()

    sleep(2000)
    println(job.state)
    runBlocking {
        job.join()
    }

    println("end")
}

private fun main() {
    nonCancellable()
}