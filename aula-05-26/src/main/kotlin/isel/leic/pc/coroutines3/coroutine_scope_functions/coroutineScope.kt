package isel.leic.pc.kotlin_concurrency.coroutine_scope_functions

import isel.leic.pc.coroutines2.list
import kotlinx.coroutines.*


private suspend fun do_in_parallel() = coroutineScope {
    println("coroutineScope corroutine")
    coroutineContext.list()

    val d1 = async {
        delay(1000)
        3
    }

    val d2 = async {
        delay(2000)
        2
    }

    d1.await() + d2.await()
}


private fun main() = runBlocking {
    println("Before")

    try {
        val res = do_in_parallel()
        println("result= $res")
    }
    catch(e: Exception) {
        println("exception on do_in_parallel: $e")
    }

}

