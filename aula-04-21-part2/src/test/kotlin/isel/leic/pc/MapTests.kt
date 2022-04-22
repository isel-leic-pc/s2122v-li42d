package isel.leic.pc

import org.junit.Assert
import org.junit.Test
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.HashMap
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.system.measureTimeMillis

/**
 * tests for checking the thread safety and efficiency of different types of
 * map implementations
 */
class MapTests {
    /**
     * the scenario simulates the generation of a word counting
     * in NLINES of text (for simplification we simulate the existence of
     * a single word in each line)
     */
    private val random = Random()

    private val NLINES = 4_000_000  // number of lines

    // for define the number of partitions (threads)
    // and the size of each slot
    private val SLOTS = IntRange(0,3).map { it*1_000_000 }.toIntArray()
    private val SLOT_SIZE = NLINES / SLOTS.size

    // the (simulated) existent words
    private val words = IntRange(0,1000).map { it.toString() }


    private fun randomInRange(min: Int, max : Int) : Int =
        Math.abs(random.nextInt()) % (max - min + 1) + min

    /**
     * this is a specialized map that have a (thread safe) operation to
     * increment the count of a given word
     */
    private class SpecializedWordCounterMap {
        val map = HashMap<String,MutableInt>()
        val mutex = ReentrantLock()

        fun increment(key: String) {
            mutex.withLock {
                map[key] = map[key]?.also { it.increment() } ?: MutableInt(1)
            }
        }
    }

    /**
     * an auxiliary function used to fill the different maps
     * given the update function received
     */
    private fun buildMap(update : (String) -> Unit) {
        val threads = mutableListOf<Thread>()

        for (index in SLOTS) {
            val localIndex = index
            val thread = thread {
                for(line in localIndex until localIndex + SLOT_SIZE) {
                   update(words.get(randomInRange(0, words.size-1)))
                }
            }
            threads.add(thread)
        }
        threads.forEach { it.join()}
    }

    /**
     * builds a map using a JVM synchronized map
     * A synchronized is just a wrapper for a non thread safe map
     * that execute each of the map operations in the possession
     * of an internal lock
     */
    private fun buildSynchronizedMap() : Map<String, MutableInt> {
        val map = Collections.synchronizedMap(HashMap<String,MutableInt>())

        buildMap {key->
            map[key] = map[key]?.also { it.increment() } ?: MutableInt(1)
            //map.computeIfAbsent(key) { MutableInt() }.increment()
        }
        return map
    }

    /**
     * A ConcurrentHashMap is a thread safe map
     * that used fine-grained lock (one per slot)
     * to increase the scalability with the increase of the number
     * of client threads
     */
    private fun buildConcurrentMap() : Map<String, MutableInt> {
        val map = ConcurrentHashMap<String,MutableInt>()
        buildMap {
            key-> map.computeIfAbsent(key) { MutableInt() }.increment()
        }
        return map
    }

    /**
     * this creates a word counter map using the SpecializedWordCounterMap
     * defined above
     */
    private fun buildSpecializedWordCounterMap() : Map<String, MutableInt> {
        val map = SpecializedWordCounterMap()
        buildMap {
            key-> map.increment(key)
        }
        return map.map
    }

    /**
     * A generic test function that evaluates and shows the execution time
     */
    private fun doTest( builder : () -> Map<String,MutableInt>, name : String) : Int{

        var count = 0
        val duration = measureTimeMillis {
            val map = builder()

            count =
                map.values.reduce  { m1, m2 ->
                    m1.add(m2)
                }.value

        }
        println("$name in $duration ms")
        return count
    }

    /**
     * first, testing with a non thread safe map
     */
    @Test
    fun `simple map test`() {
        val expectedCount = NLINES

        val map = HashMap<String,Int>()
        val threads = mutableListOf<Thread>()

        for (index in SLOTS) {
            val localIndex = index
            val thread = thread {
                for(line in localIndex until localIndex + SLOT_SIZE) {
                    val key = words.get(randomInRange(0, words.size-1))
                    var count =  map[key]
                    map[key] =  if (count == null) 1 else count +1
                }
            }
            threads.add(thread)
        }
        threads.forEach { it.join()}

        val count =
            map.values.reduce  { c1, c2 ->
               c1+c2
            }
        Assert.assertEquals(expectedCount, count)
    }

    @Test
    fun `synchronized map test`() {

        val expectedCount = NLINES
        val count = doTest(::buildSynchronizedMap, "synchronized map test")

        Assert.assertEquals(expectedCount, count)

    }


    @Test
    fun `concurrent map test`() {
        val expectedCount = NLINES
        val count = doTest(::buildConcurrentMap, "concurrent map test")

        Assert.assertEquals(expectedCount, count)

    }

    @Test
    fun `specialized word counter map test`() {
        val expectedCount = NLINES
        val count = doTest(::buildSpecializedWordCounterMap, "specialized word counter map test")

        Assert.assertEquals(expectedCount, count)
    }

}