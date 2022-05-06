package isel.leic.pc.servers

import mu.KotlinLogging
import java.lang.Thread.sleep
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future

private val logger = KotlinLogging.logger {}

val executor =
    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())


fun readTextFileAsync(fileName: String) : Future<String> {
    val bufferSize = 4096
    val buffer = ByteBuffer.allocate(bufferSize)
    val channel = AsynchronousFileChannel.open(Path.of(fileName),
                    StandardOpenOption.READ)
    val charSet = Charset.defaultCharset()
    val decoder = charSet.newDecoder()

    channel.use {
       channel.read(buffer, 0L).get()
    }

    return executor.submit<String> {
        buffer.flip()
        decoder.decode(buffer).toString()
    }
}

fun readTextFileAsync(fileName: String,
                  continuation : (String?,Throwable?) -> Unit) : Unit  {
    val bufferSize = 4096
    val channel = AsynchronousFileChannel.open(Path.of(fileName), StandardOpenOption.READ)
    val charSet = Charset.defaultCharset()
    val decoder = charSet.newDecoder()
    val buffer = ByteBuffer.allocate(bufferSize)

    val handler = object : CompletionHandler<Int?, Any?> {
        override fun completed(result: Int?, attachment: Any?) {
            buffer.flip()
            channel.close()
            continuation(decoder.decode(buffer).toString(), null)
        }

        override fun failed(exc: Throwable?, attachment: Any?) {
            channel.close()
            continuation(null, exc)
        }

    }
    channel.read(buffer, 0L, null, handler )
}

private fun main() {
    println(readTextFileAsync("asyncUtils.kt").get())
    /*
    val cdl = CountDownLatch(1)
    readTextFileAsync("asyncUtils.kt") {
            content: String?, err: Throwable? ->
        sleep(5000)
        println(content ?: err!!.message)
        cdl.countDown()
    }

    cdl.await()
    */
}



