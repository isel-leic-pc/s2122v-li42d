package isel.leic.pc.coroutines2.jobs


import isel.leic.pc.coroutines2.list
import kotlinx.coroutines.*
import java.lang.Thread.sleep


fun parents_and_childs_in_global_scope() {
    val job : Job = GlobalScope.launch {

        val parentJob = coroutineContext.job


        println("parent scope:")
        this.list()

        launch {
            println("child scope:")
            list()
            print("is current job equals to parent job? ")
            println(parentJob == coroutineContext.job)
            print("is parent job child equals to current job? ")
            println(parentJob.children.first() == coroutineContext.job)
        }
    }
    runBlocking {
        job.join()
    }


}


fun parents_and_childs_in_run_blocking() {
    runBlocking {
        val parentJob = coroutineContext.job

        println("parent scope:")
        this.list()

        launch(Dispatchers.Default) {
            println("child scope:")
            list()
            print("is current job equals to parent job? ")
            println(parentJob == coroutineContext.job)
            print("is parent job child equals to current job? ")
            println(parentJob.children.first() == coroutineContext.job)
        }

    }
}

private fun main() {
    println("Start")

    parents_and_childs_in_global_scope()


    println("End")
}