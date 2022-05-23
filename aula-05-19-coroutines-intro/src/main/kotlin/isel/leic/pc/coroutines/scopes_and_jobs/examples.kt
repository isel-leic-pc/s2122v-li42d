package isel.leic.pc.coroutines.scopes_and_jobs

import kotlinx.coroutines.*

private fun main() {
    val job = Job()

    runBlocking {
        val job1 = GlobalScope.launch {
            delay(1000)
            println("Hello")
        }

        val job2 = GlobalScope.launch {
            delay(1000)
            println(", World")
        }
        job1.join()
        job2.join()
    }
    println("all done!")
}