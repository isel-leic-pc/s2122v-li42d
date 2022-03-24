package isel.leic.pc

import kotlin.concurrent.thread

private fun main() {
    val t = thread(isDaemon = true, start = false) {
        Thread.sleep(2000)
        println("hello from thread ${Thread.currentThread().name}")
    }
    t.start()
    t.join()

    println("main thread name: ${Thread.currentThread().name}")
}