package i2020_ep1

import data.T
import isel.leic.pc.kotlin_concurrency.utils.elements
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.lang.Thread.sleep
import kotlin.system.measureTimeMillis
private val logger = KotlinLogging.logger {}

fun a(t : T)  : T {
    sleep(1000)
    return t
}

fun b(t: T) : T {
    sleep(1000)
    return t
}

fun c(t1: T, t2: T) : T {
    sleep(1000)
    return t1
}

fun d(t: T, acc : T) : T {
    sleep(1000)
    return acc
}

fun oper(xs: Array<T>, ys: Array<T>, initial: T) : T
{
    if (xs.size != ys.size) throw  IllegalArgumentException("...");
    var acc = initial;
    for (i in 0 until xs.size)
    {
        acc = d(c(b(a(xs[i])), b(a(ys[i]))), acc);
    }
    return acc;
}

suspend fun a1(t : T) : T {
    delay(1000)
    return t
}

suspend fun b1(t: T) : T  {
    delay(1000)
    return t
}

suspend fun c1(t1: T, t2: T) : T  {
    delay(1000)
    return t1
}

suspend fun d1(t: T, acc : T) : T {
    delay(1000)
    return acc
}

suspend fun oper1(xs: Array<T>, ys: Array<T>, initial: T) : T =
                        coroutineScope {
    if (xs.size != ys.size) throw  IllegalArgumentException("...");
    var acc = initial;
    logger.info(coroutineContext.elements())
    var cDefs : Array<Deferred<T>?> = Array(xs.size) { null }
    for (i in 0 until xs.size) {
        val rc = async {
            logger.info(coroutineContext.elements())
            val d1 = async {
                b1(a1(xs[i]))
            }
            val d2 = async {
                b1(a1(ys[i]))
            }

            c1(d1.await(), d2.await())
        }
        cDefs[i] = rc
    }
    for (i in 0 until xs.size)
        acc = d1(cDefs[i]!!.await(), acc)
    acc
}

// change line 89
// from: private fun main() = runBlocking {
// to:   private suspend fun main() = coroutineScope
// and justify the differences and similarities in output
private fun main() = runBlocking {
    val time = measureTimeMillis {
        val res = oper1(Array<T>(3) { T(0) },
              Array<T>(3) { T(0) },
            T(0)
        )
        println(res)
    }
    println("done in $time ms")
}

