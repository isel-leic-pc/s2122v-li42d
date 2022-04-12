package isel.leic.pc.monitors4

import mu.KLogger
import mu.KotlinLogging
import org.junit.Assert
import org.junit.Test
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration


private val logger : KLogger? = KotlinLogging.logger {}

class Series1Tests {
    /**
     * this simple test presents most of the considered best practices
     * used on concurrency tests construction
     *
     * - first, create the artifacts needed
     * - second, launch the thread(s) that executes the (potentially blocking) code to test
     * - start building simple tests that test single operations on a simple context
     * - the created thread(s) should not execute any assertion.
     *   they just create the needed result artifacts
     * - the test thread collects the results produces joining the created threads(s)
     *   with an appropriate timeout to avoid running tests undefinable times
     * - finally the test thread runs the necessary assertions to check the
     *   test success
     */
    @Test
    fun `blocking message single dequeue on an initial empty queue`() {
        // build the needed artifacts
        val capacity = 1
        val msgQueue = BlockingMessageQueue<String>(capacity)
        val expectedResult = "Ok"
        var result : String? = null

        // create a thread that run the code to test and produce the necessary results
        val t = thread {
            result = msgQueue.tryDequeue(
                1000.toDuration(DurationUnit.MILLISECONDS))
        }

        // code that resolve the operation done in the created thread
        // In more complex scenarios this should run on another created test,
        // but in this code this seems unnecessary
        msgQueue.tryEnqueue(listOf(expectedResult), Duration.ZERO);

        // join the created threads e«with a timeout
        t.join(2000)

        // do the necessary assertions
        Assert.assertEquals(expectedResult, result)
    }

    /**
     * A more complicated test that should be done after the simple tests
     * already succeed
     * In this case we have an arbitrary number of enqueuers  and dequeuers
     * the test parametrizes the number of each.
     * Initially it should be parametrized to the most simple situation, NREADERS=NWRITERS=1
     * Varying capacity can test scenarios where enqueuers need to block
     */
    @Test
    fun blocking_message_multiple_send_receive() {
        val NWRITERS = 4
        val NREADERS = 3
        val CAPACITY = 5


        val listOfLists = listOf(
            listOf(1,2,3),
            listOf(4,5),
            listOf(6),
            listOf(7,8,9,10),
            listOf(11, 12, 13),
            listOf(14,15,16,17),
            listOf(18,19,20))

        val RESULT_SIZE =
            listOfLists
            .flatMap { it }
            .count()

        val msgQueue = BlockingMessageQueue<Int>(CAPACITY)

        // the result queue is used for multiple readers support
        // we can also use a thread safe set collection but we
        // talk about thread safe collections later
        val resultQueue = BlockingMessageQueue<Int?>(RESULT_SIZE)

        // this index use is protected by a mutex
        // in oprder to support multiple writers
        var writeInputIndex = 0
        val mutex = ReentrantLock()


        val writerThreads = mutableListOf<Thread>()
        val readerThreads = mutableListOf<Thread>()

        repeat(NWRITERS) {
            val thread = thread {
                logger?.info("start writer")
                while(true) {
                    var localIdx = listOfLists.size
                    mutex.withLock {
                        if (writeInputIndex <  listOfLists.size) {
                            localIdx = writeInputIndex++
                        }
                    }
                    if (localIdx < listOfLists.size ) {
                        val value = listOfLists.get(localIdx)
                        logger?.info("writer try send $value")
                        if (msgQueue.tryEnqueue(value, Duration.INFINITE))
                            logger?.info("writer send $value")
                    }
                    else {
                        break;
                    }

                }
                logger?.info("end writer")
            }
            writerThreads.add(thread)
        }

        repeat(NREADERS) {
            logger?.info("start reader}")
            val thread = thread {
                while(true) {
                    val value =
                        msgQueue.tryDequeue(2000.toDuration(DurationUnit.MILLISECONDS)) ?: break
                    if (resultQueue.tryEnqueue(listOf(value), Duration.INFINITE))
                        logger?.info("reader get $value")
                }
                logger?.info("end reader}")
            }
            readerThreads.add(thread)
        }

        // wait for writers termination (with a timeout)
        for(wt in writerThreads) {
            wt.join(3000)
            if (wt.isAlive) {
                Assert.fail("too much execution time for writer thread")
            }
        }

        // wait for readers termination (with a timeout)
        for(rt in readerThreads) {
            rt.join(3000)
            if (rt.isAlive) {
                Assert.fail("too much execution time for reader thread")
            }
        }

        val resultSet = TreeSet<Int>()

        repeat(RESULT_SIZE) {
            val r = resultQueue.tryDequeue(Duration.ZERO)
            if (r == null) {
                Assert.fail("failed tryDequeue operation on result queue")
            }
            resultSet.add(r!!)
        }

        Assert.assertEquals(RESULT_SIZE, resultSet.size)
        var previous = 0
        for (v in resultSet) {
            Assert.assertEquals(previous+1, v)
            previous = v
        }
    }
}