package i2018_ep1

import data.R
import data.T
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.lang.Thread.sleep
import kotlin.random.Random
import kotlin.system.measureTimeMillis


fun map(elem: T) : R {
   sleep(1000)
   return  R()
}

fun reduce(r1: R, r2: R) : R {
    sleep(500)
    return r1
}

fun mapReduce( elems: List<T>, initial : R) : R {
    var curr = initial
    for(i in 0 until elems.size) {
        curr = reduce(map(elems[i]), curr)
    }
    return curr
}

// suspend version

val random = Random.Default

suspend fun map1(elem: T) : R {
    delay(random.nextLong(10000))
    return R()
}

suspend fun  reduce1(r1: R, r2: R) : R {
    delay(500)
    return r1
}

suspend fun mapReduce1( elems: List<T>, initial : R) : R  = coroutineScope{
    var curr = initial

    elems
    .map {
        async {
            map1(it)
        }
    }
    .map {
        it.await()
    }
    .fold(initial) { accum, it ->
        reduce1(accum, it)
    }

}


private suspend fun main() = coroutineScope{
    val time = measureTimeMillis {

        val result = mapReduce1(listOf( T(1),T(2), T(3), T(4)), R())

    }

    println("done in $time ms.")
}