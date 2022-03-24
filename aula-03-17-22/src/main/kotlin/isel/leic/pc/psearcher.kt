package isel.leic.pc

import kotlin.concurrent.thread
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

fun search(values: Array<String>, ref: String) : Int {
    var total = 0
    for (i in 0..values.size-1)
        if (ref.equals(values[i]))
            total++
    return total
}

fun psearch(values: Array<String>, ref: String) : Int {
    val nprocs = Runtime.getRuntime().availableProcessors()
    val psize = values.size / nprocs
    val threads = ArrayList<Thread>(nprocs)
    val partialResults = IntArray(nprocs) {0}
    var globalStart = 0

    for(i in 0 .. nprocs-1) {
        val start = globalStart
        val localIndex = i
        val end = if (start + psize < values.size)
                    start + psize -1
                  else
                    values.size -1
        val t = thread {
            var count =0;
            for (idx in start..end) {
                if (values[idx].equals(ref))
                    count++
            }
            partialResults[localIndex] = count
        }
        globalStart += psize
        threads.add(t)
    }

    var finalCount = 0
    for( i in 0..nprocs-1) {
        threads[i].join()
        finalCount += partialResults[i]
    }

    return finalCount

}

fun test( function : (Array<String>, String) -> Int,
          values: Array<String>,
          ref: String, prefix: String) {
    var minTime = Long.MAX_VALUE
    var total = 0
    repeat(5) {
        var curTotal : Int
        val time = measureNanoTime{
            curTotal =  function(values, ref)
        }
        if (time < minTime) {
            minTime = time
            total = curTotal
        }
    }

    println("$prefix find=$total in ${minTime} nanos!")
}

private fun buildString() : String {
    val sb = StringBuilder()

    for( i in 0..70000)
        sb.append('a' + (i % 24))

    return sb.toString()
}


private fun main() {
    val s = buildString()
    val values = Array<String>(1000_000) { s }

    test(::search, values,buildString(), "serial" )
    test(::psearch, values, buildString(), "parallel" )

}