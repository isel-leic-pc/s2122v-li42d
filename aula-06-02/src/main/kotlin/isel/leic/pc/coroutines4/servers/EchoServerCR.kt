package isel.leic.pc.coroutines4.servers


import isel.leic.pc.coroutines2.list
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousChannelGroup
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.charset.Charset
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext


private val logger = KotlinLogging.logger {}
private val charSet = Charset.defaultCharset()
private val decoder = charSet.newDecoder()

/**
 * Echo server using a coroutines API to NIO2
 */

class EchoServerCR(private val port : Int) {
    private val exitCmd = "exit"
    private val byeMsg = "bye" + System.lineSeparator()

    private class ServerScope {

        private val handler = CoroutineExceptionHandler { ctx, exc ->
            logger.info("caught exception $exc: ${exc.message}")
        }

        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + handler)
    }

    private val group =
        AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor())
    private val serverChannel = AsynchronousServerSocketChannel.open(group)

    // The parent scope to handler coroutines
    private val scope = ServerScope().scope

     suspend fun handler(clientChannel: AsynchronousSocketChannel) {
        val bufSize = 4096
        // the buffer used for async I/O via nio2
        // note that we can use a single buffer since there is only
        // a single operation in course at a given time
        val buffer = ByteBuffer.allocate(bufSize)

        fun isExitCmd() : Boolean {
            val text = decoder.decode(buffer).toString()
            buffer.flip()
            return text.equals(exitCmd)
        }

        // send the bye message to client
        suspend fun  bye() {
            putBuffer(buffer, byeMsg)
            write(clientChannel, buffer)
        }

        suspend fun process()  {
            logger.info("Start client processing")
            while(read(clientChannel, buffer) > 0) {
                buffer.flip()
                logger.info("msg read from ${clientChannel.remoteAddress}")
                if (isExitCmd()) {
                    bye()
                    break
                }
                write( clientChannel, buffer)
                logger.info("After echo message")
                buffer.clear()
            }
        }

        try {
            process()
        }
        catch(e: Exception) {
            println("error on client handler: $e")
        }
        finally {
            closeConnection(clientChannel)
        }
    }

    fun run() {
        suspend fun runInternal() {
            while(true) {
                val channel = accept(serverChannel)
                logger.info("client ${channel.remoteAddress} connected")

                // connection handler coroutine
                scope.launch {
                    handler(channel)
                }

                // to debug purposes
                //scope.coroutineContext.job.childrenStates()
            }
        }

        serverChannel.bind(InetSocketAddress("0.0.0.0", port))

        // accept connections coroutine
        scope.launch {
            try {
                runInternal()
            }
            catch(e: Exception) {
                println("error on accept: terminate server!")
            }
            finally {
                serverChannel.close()
            }
        }
    }

    fun stop() {
        serverChannel.close()
        sleep(1000)
        group.shutdown()
        group.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
    }
}

private fun main() {
    val server = EchoServerCR(8080)
    server.run()

    readln()
    server.stop()
    logger.info("Server terminated")
}