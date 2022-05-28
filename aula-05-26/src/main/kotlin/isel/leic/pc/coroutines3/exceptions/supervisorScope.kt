package isel.leic.pc.coroutines3.exceptions

import isel.leic.pc.coroutines2.childrenStates
import isel.leic.pc.coroutines2.list
import isel.leic.pc.coroutines2.state
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.lang.Thread.sleep
import kotlin.coroutines.CoroutineContext

private val logger= KotlinLogging.logger {}


private suspend fun multiple_child_coroutines(context: CoroutineContext) {
    val scope = CoroutineScope(context)
    scope.list()

    scope.launch {
        delay(1000)
        println("throw some error")
        throw Error("some error")
    }

    scope.launch {
        delay(3000)
        println("ok!")
    }

}

private suspend fun multiple_child_coroutines_from_normal_job() {
    multiple_child_coroutines(Job() + Dispatchers.Default)
}

private suspend fun multiple_child_coroutines_from_supervisor_job() {
    val handler = CoroutineExceptionHandler { ctx, exc ->
        println("caught exception $exc")
    }
    val context = SupervisorJob() + Dispatchers.Default + handler
    multiple_child_coroutines(context)
}

private suspend fun multiple_child_coroutines_from_supervisorScope() {
    supervisorScope {
        launch {
            delay(1000)
            println("throw some error")
            throw Error("some error")
        }

        launch {
            delay(3000)
            println("ok!")
        }

        delay(5000)
    }
}

private fun main()  = runBlocking {
    multiple_child_coroutines_from_supervisor_job()
    delay(5000)
    println("Done")
}