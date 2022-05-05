package isel.leic.pc.servers

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

class EchoServerAsyncCF(private val port : Int) {
    private val BUFSIZE = 1024

    private fun handler(connection : AsynchronousSocketChannel, connectionId: Int ) {
        val buffer = ByteBuffer.allocate(BUFSIZE)
        val charSet = Charset.defaultCharset()
        val decoder = charSet.newDecoder()

        fun process() : CompletableFuture<Int> {
           return   connection.readAsync(buffer)
                    .thenCompose{
                        if (it <= 0) {
                            connection.close()
                            logger.info("connection closed!")
                            val promise = CompletableFuture<Int>()
                            promise.complete(it)
                            promise
                        }
                        else {
                            buffer.flip()
                            val text = decoder.decode(buffer).toString()

                            logger.info("message '$text' received with size $it")
                            buffer.flip()
                            connection.writeAsync(buffer)
                            .thenCompose {
                                buffer.clear()
                                process()
                            }
                        }
                    }
               .exceptionally() {
                   logger.info("exception   $it")
                   connection.close()
                   -1
               }

        }

        process()
    }


    fun run() {
        val group =
            AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(1))
        var listeningSock =
            AsynchronousServerSocketChannel.open(group)

        listeningSock.bind(InetSocketAddress("0.0.0.0", port))

        fun acceptConnection(sessionId : Int) {
            listeningSock.acceptAsync()
                .thenApply {
                    logger.info("client ${it.remoteAddress} connected")
                    handler(it, sessionId)
                    acceptConnection(sessionId + 1 )
                }
        }

        acceptConnection(1)

    }
}

private fun main() {
    val server = EchoServerAsyncCF(8080)
    server.run()

    readln()
}