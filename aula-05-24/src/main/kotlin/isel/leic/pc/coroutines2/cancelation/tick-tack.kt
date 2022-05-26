package isel.leic.pc.coroutines2.cancelation

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Thread.sleep
import kotlin.random.Random

fun tick_tack() {
    val job1 = GlobalScope.launch {
        while(true) {
            delay(1000)
            println("tick")
        }
    }

    GlobalScope.launch {
        while(true) {
            delay(1000)
            println("tack")
        }
    }

    sleep(6000)
    job1.cancel()

}


fun tick_tack2() {
    runBlocking {
        val parent = launch {

            launch {
                while (true) {
                    delay(1000)
                    println("tick")
                }
            }

            launch {

                while (true) {
                    delay(1000)
                    println("tack")
                }
            }
        }
        delay(6000)
        parent.cancel()
        parent.join()
    }
}

private fun main() {
    println("start")
    tick_tack2()

    println("end")
}

