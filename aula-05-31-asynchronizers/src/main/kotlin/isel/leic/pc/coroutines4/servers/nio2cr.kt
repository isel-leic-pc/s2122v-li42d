package isel.leic.pc.coroutines4.servers

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import kotlin.random.Random

private val random = Random.Default

/**
 * Coroutine Api to NIO2 asynchronous socket channels
 */
interface Nio2Api {
    suspend fun read(channel: AsynchronousSocketChannel,
                     buffer: ByteBuffer) : Int

    suspend fun write(channel: AsynchronousSocketChannel,
                     buffer: ByteBuffer) : Int

    suspend fun accept(listener : AsynchronousServerSocketChannel)
            : AsynchronousSocketChannel
}

/**
 * The completion handler implementation for reads
 */
val readHandler = object : CompletionHandler<Int, CancellableContinuation<Int>> {
    override fun completed(result: Int, attachment: CancellableContinuation<Int>) {
            attachment.resume(result)
    }

    override fun failed(exc: Throwable, attachment: CancellableContinuation<Int>) {
        attachment.resumeWithException(exc)
    }
}


class Nio2ApiImpl : Nio2Api {

    override suspend fun read(channel: AsynchronousSocketChannel, buffer: ByteBuffer): Int {
        return suspendCancellableCoroutine { cont->
            // close channel on cancellation
            cont.invokeOnCancellation {
                channel.close()
            }

            channel.read(buffer, cont, readHandler)
        }
    }

    override suspend fun write(channel: AsynchronousSocketChannel, buffer: ByteBuffer): Int {
       TODO()
    }

    override suspend fun accept(listener: AsynchronousServerSocketChannel): AsynchronousSocketChannel {
       TODO()
    }

}