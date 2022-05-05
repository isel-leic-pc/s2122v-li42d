package servers

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.CompletionHandler
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.Executors
import java.util.concurrent.Future

val executor =
    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

fun readTextFileAsync(fileName: String) : Future<String> {
    val bufferSize = 4096
    val channel = AsynchronousFileChannel.open(Path.of(fileName), StandardOpenOption.READ)

    val charSet = Charset.defaultCharset()
    val decoder = charSet.newDecoder()
    val buffer = ByteBuffer.allocate(bufferSize)

    return executor.submit<String> {
        val res = channel.read(buffer, 0L).get()
        decoder.decode(buffer).toString()
    }

}

private fun main() {
    val res = readTextFileAsync("input.txt")
    println(res.get())
}