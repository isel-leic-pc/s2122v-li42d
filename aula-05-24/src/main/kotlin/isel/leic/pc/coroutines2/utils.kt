package isel.leic.pc.coroutines2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

val Job.state : String
    get() {
        if (isCancelled) return "Cancelled"
        if (isCompleted) return "Completed"
        if (isActive)    return "Active"
        return "Unknown"
    }

fun Job.childrenStates() {
    children.forEach {
        println("State: ${state}")
    }
}

fun CoroutineContext.list() {

    val list = mutableListOf<Pair<CoroutineContext,Any>>()

    fold(list) {
            l,c ->  l.add(Pair(c, c.key.javaClass)); l
    }
    println("Context ${Math.abs(this.hashCode())}:")
    list.forEach {
        println(it)
    }
}

fun CoroutineScope.list() {
    println("Scope ${Math.abs(this.hashCode())}:")
    coroutineContext.list()
}