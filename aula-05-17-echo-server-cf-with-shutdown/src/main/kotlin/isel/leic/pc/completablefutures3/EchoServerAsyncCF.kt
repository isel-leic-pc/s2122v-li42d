package isel.leic.pc.completablefutures3

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
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess


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

    // to support controlled shutdown
    val terminated = CompletableFuture<Unit>()
    private val nConnections = AtomicInteger(0)

    private val startedShutdown = AtomicBoolean(false)

    val group =
        AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor())
    val serverChannel : AsynchronousServerSocketChannel

    init {
        serverChannel = AsynchronousServerSocketChannel.open(group)
        serverChannel.bind(InetSocketAddress("0.0.0.0", port))
    }

    fun run() {
        // using a backed threadpool with a single thread
        // this remarks (while not identical) the node execution environment


        // asynchronous accept loop
        fun runInternal() {
            if (startedShutdown.get()) {
                serverChannel.acceptAsync()
                    .thenApply {
                        nConnections.incrementAndGet()
                        logger.info("new client: ${nConnections.get()} clients")
                        clientHandler(it)
                            .thenAccept {
                                logger.info(
                                    "client terminated:  ${nConnections.get()} clients!")
                                val result = nConnections.decrementAndGet()
                                if (startedShutdown.get() && result == 0)
                                    terminated.complete(Unit)

                            }
                        runInternal()
                    }
                    .whenComplete {
                            v, e ->
                        if (e != null)
                            logger.info("error on accept:  ${e.message}")
                        startShutdown()
                            .whenComplete {
                                    v, e ->
                                exitProcess(0)
                            }
                    }
            }

        }

        runInternal()
    }

    fun clientHandler(clientChannel: AsynchronousSocketChannel) :
             CompletableFuture<Unit> {
        val bufSize = 4096
        // the buffer used for async I/O via nio2
        // note that we can use a single buffer since there is only
        // a single operation in course at a given time
        val buffer = ByteBuffer.allocate(bufSize)
        val completedFuture =
            CompletableFuture.completedFuture(Unit)
        // auxiliary function to check the exit cmd
        fun isExitCmd() : Boolean {
            val text = decoder.decode(buffer).toString()
            buffer.flip()
            return text.equals(exitCmd)
        }

        // send the bye message to client
        fun bye() : CompletableFuture<Unit> {
            putBuffer(buffer, byeMsg)
            return clientChannel.writeAsync(buffer)
                .thenApply {
               closeConnection(clientChannel)
            }
        }

        // asynchronous client handler processing
        fun process() : CompletableFuture<Unit> {
            return clientChannel.readAsync(buffer)
            .thenCompose {
                if (it <= 0) {
                    // the client has closed the connection, so do we
                    closeConnection(clientChannel)
                    completedFuture
                }
                else {
                    buffer.flip()
                    logger.info("msg read from ${clientChannel.remoteAddress}")
                    if (isExitCmd()) {
                        bye()
                    }
                    else {
                        clientChannel.writeAsync(buffer)
                        .thenCompose {
                            buffer.clear()
                            logger.info("echo send from ${clientChannel.remoteAddress}")
                            process()
                        }

                    }
                }
            }
            .exceptionally {
                closeConnection(clientChannel)
            }

        }

        return process()
    }

    /**
     * shutdown processing
     * To be done in the next lecture
     */
    fun startShutdown() : CompletableFuture<Unit> {
        logger.info("Shutdown started")
        if (startedShutdown.compareAndSet(false, true)) {
            logger.info("Shutdown process")
            serverChannel.close()
            group.shutdown()
        }

        return terminated
    }
}

private fun main() {
    val server = EchoServerAsyncCF(8080)
    server.run()

    readln()

    server.startShutdown().get()
    logger.info("Server terminated")
}