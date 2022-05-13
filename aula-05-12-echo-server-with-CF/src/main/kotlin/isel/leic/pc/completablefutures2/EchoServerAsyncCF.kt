package isel.leic.pc.completablefutures2

import isel.leic.pc.servers.closeConnection
import isel.leic.pc.servers.putBuffer
import mu.KotlinLogging
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousChannelGroup
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.charset.Charset
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors


private val logger = KotlinLogging.logger {}
private val charSet = Charset.defaultCharset()
private val decoder = charSet.newDecoder()

/**
 * Echo server using the read/write asynchronous extensions
 * for nio2 asynchronous sockets
 */

class EchoServerAsyncCF(private val port : Int) {
    private val bufSize  = 1024
    private val exitCmd = "exit"
    private val byeMsg = "bye" + System.lineSeparator()
    private val charSet = Charset.defaultCharset()
    private val decoder = charSet.newDecoder()


    fun run() {
        // using a backed threadpool with a single thread
        // this remarks (while not identical) the node execution environment
        val group =
            AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor())

        val serverChannel = AsynchronousServerSocketChannel.open(group)
        serverChannel.bind(InetSocketAddress("0.0.0.0", port))

        // asynchronous accept loop
        fun runInternal() {
            serverChannel.acceptAsync()
                .thenApply {
                    clientHandler(it)
                    runInternal()
                }
        }

        runInternal()
    }

    fun clientHandler(clientChannel: AsynchronousSocketChannel) {
        val bufSize = 4096
        // the buffer used for async I/O via nio2
        // note that we can use a single buffer since there is only
        // a single operation in course at a given time
        val buffer = ByteBuffer.allocate(bufSize)

        // auxiliary function to check the exit cmd
        fun isExitCmd() : Boolean {
            val text = decoder.decode(buffer).toString()
            buffer.flip()
            return text.equals(exitCmd)
        }

        // send the bye message to client
        fun bye() {
            putBuffer(buffer, byeMsg)
            clientChannel.writeAsync(buffer)
                .thenApply {
               closeConnection(clientChannel)
            }
        }

        // asynchronos client handler processing
        fun process() {
            clientChannel.readAsync(buffer)
            .thenApply {
                if (it <= 0) {
                    // the client has closed the connection, so do we
                    closeConnection(clientChannel)
                }
                else {
                    buffer.flip()
                    logger.info("msg read from ${clientChannel.remoteAddress}")
                    if (isExitCmd()) {
                        bye()
                    }
                    else {
                        clientChannel.writeAsync(buffer)
                        .thenApply {
                            buffer.clear()
                            logger.info("echo send from ${clientChannel.remoteAddress}")
                            process()
                        }
                    }
                }
            }
        }

        process()
    }

    /**
     * shutdown processing
     * To be done in the next lecture
     */
    fun startShutdown() : CompletableFuture<Unit> {
        TODO()
    }
}

private fun main() {
    val server = EchoServerAsyncCF(8080)
    server.run()

    readln()

    server.startShutdown().get()
}