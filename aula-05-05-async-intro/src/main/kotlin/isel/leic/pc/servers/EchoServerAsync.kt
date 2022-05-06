package isel.leic.pc.servers

import mu.KotlinLogging
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousChannelGroup
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.nio.charset.Charset
import java.util.concurrent.Executors


private val logger = KotlinLogging.logger {}
private val charSet = Charset.defaultCharset()
private val decoder = charSet.newDecoder()



class EchoServerAsync(private val port : Int) {
    private val bufSize  = 1024
    private val exitCmd = "exit"
    private val byeMsg = "bye" + System.lineSeparator()
    private val charSet = Charset.defaultCharset()
    private val decoder = charSet.newDecoder()


    fun run() {

        val group =
            AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor())

        val serverChannel = AsynchronousServerSocketChannel.open(group)
        serverChannel.bind(InetSocketAddress("0.0.0.0", port))

        while(true) {
            val clientChannel = serverChannel.accept().get()
            logger.info("client ${clientChannel.remoteAddress} connected")

            // asynchronous client handler
            clientHandler(clientChannel)
        }

    }

    fun clientHandler(clientChannel: AsynchronousSocketChannel) {
        val bufSize = 4096
        // the buffer used for async I/O via nio2
        // note that we can use a single buffer since there is only
        // a single operation in course at a given time
        val buffer = ByteBuffer.allocate(bufSize)

        // auxiliary function to execute an asynchronous read
        // using CompletionHandler method
        fun read(continuation : (Int) -> Unit) {
            val handler = object : CompletionHandler<Int, Any?> {
                override fun completed(result: Int, attachment: Any?) {
                     buffer.flip()
                     continuation(result)
                }

                override fun failed(exc: Throwable?, attachment: Any?) {
                    logger.info("read error on client ${clientChannel.remoteAddress}")
                    continuation(-1)
                }
            }
            clientChannel.read(buffer, null, handler)
        }

        // auxiliary function to execute an asynchronous write
        // using CompletionHandler method
        fun write(continuation : () -> Unit) {
            val handler = object : CompletionHandler<Int, Any?> {
                override fun completed(result: Int, attachment: Any?) {
                    // prepare buffer for next read
                    buffer.clear()
                    continuation()
                }

                override fun failed(exc: Throwable?, attachment: Any?) {
                    // ignoring write errors for simplicity
                    logger.info("write error on client ${clientChannel.remoteAddress}")
                    buffer.clear()
                    continuation()
                }
            }
            clientChannel.write(buffer, null, handler)
        }

        // auxiliary function to check the exit cmd
        fun isExitCmd() : Boolean {
            val text = decoder.decode(buffer).toString()
            buffer.flip()
            return text.equals(exitCmd)
        }

        // send the bye message to client
        fun bye() {
            putBuffer(buffer, byeMsg)
            write {
               closeConnection(clientChannel)
            }
        }

        fun process() {
            read {
                if (it <= 0) {
                    // the client has closed the connection, so do we
                    closeConnection(clientChannel)
                }
                else {
                    logger.info("msg read from ${clientChannel.remoteAddress}")
                    if (isExitCmd()) {
                        bye()
                    }
                    else {
                        write {
                            logger.info("echo send from ${clientChannel.remoteAddress}")
                            process()
                        }
                    }
                }
            }
        }

        process()
    }
}

private fun main() {
    val server = EchoServerAsync(8080)
    server.run()
}