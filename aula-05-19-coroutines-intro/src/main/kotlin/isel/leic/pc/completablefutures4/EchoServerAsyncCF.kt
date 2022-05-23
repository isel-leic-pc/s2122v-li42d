package isel.leic.pc.completablefutures4

import isel.leic.pc.servers.closeConnection
import isel.leic.pc.servers.putBuffer
import mu.KotlinLogging
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousChannelGroup
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.charset.Charset
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.system.exitProcess


private val logger = KotlinLogging.logger {}
private val charSet = Charset.defaultCharset()
private val decoder = charSet.newDecoder()

/**
 * Echo server using the read/write asynchronous extensions
 * for nio2 asynchronous sockets
 */

class EchoServerAsyncCF( port : Int) {
    private val exitCmd = "exit"
    private val byeMsg = "bye" + System.lineSeparator()

    // to support controlled shutdown
    val lifeManager = LifeManager {
        logger.info("Shutdown process")

        serverChannel.close()
        sleep(2000)
        group.shutdown()
    }

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
            if (lifeManager.inShutdown) return
            serverChannel.acceptAsync()
            .thenApply {
                if (lifeManager.enter()) {
                    logger.info("new client: ${lifeManager.count} clients")
                    clientHandler(it)
                        .whenComplete { v, error ->
                            val count = lifeManager.leave()
                            logger.info(
                                "client terminated:  $count clients!"
                            )
                        }

                    runInternal()
                }
            }
            .exceptionally {
                logger.info("error on accept:  ${it.message}")
                lifeManager.shutdown()
                    .thenAccept {
                        exitProcess(0)
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
                        .exceptionally {
                            closeConnection(clientChannel)
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
    fun startShutdown() : CompletableFuture<Boolean> {
        logger.info("Shutdown started")
        return lifeManager.shutdown()
    }
}

private fun main() {
    val server = EchoServerAsyncCF(8080)
    server.run()

    readln()

    server.startShutdown().get()
    logger.info("Server terminated")
}