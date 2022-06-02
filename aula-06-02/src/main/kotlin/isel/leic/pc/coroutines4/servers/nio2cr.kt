package isel.leic.pc.coroutines4.servers

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.*
import kotlinx.coroutines.suspendCancellableCoroutine
import mu.KotlinLogging
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import kotlin.random.Random

private val random = Random.Default
private val logger = KotlinLogging.logger {}

/**
 * The completion handler implementation for reads
 */
private val readHandler = object : CompletionHandler<Int, CancellableContinuation<Int>> {
    override fun completed(result: Int, attachment: CancellableContinuation<Int>) {
        logger.info("on read callback")
        attachment.resume(result)
    }

    override fun failed(exc: Throwable, attachment: CancellableContinuation<Int>) {
        attachment.resumeWithException(exc)
    }
}

private val writeHandler = object : CompletionHandler<Int, CancellableContinuation<Int>> {
    override fun completed(result: Int, attachment: CancellableContinuation<Int>) {
        logger.info("on write callback")
        attachment.resume(result)
    }

    override fun failed(exc: Throwable, attachment: CancellableContinuation<Int>) {
        attachment.resumeWithException(exc)
    }
}

private val acceptHandler = object :
            CompletionHandler<AsynchronousSocketChannel,
            CancellableContinuation<AsynchronousSocketChannel>> {
    override fun completed(result: AsynchronousSocketChannel,
                           attachment: CancellableContinuation<AsynchronousSocketChannel>) {
        logger.info("on accept callback")
        attachment.resume(result)
    }

    override fun failed(exc: Throwable,
                        attachment: CancellableContinuation<AsynchronousSocketChannel>) {
        attachment.resumeWithException(exc)
    }
}

// extension methods for using nio2 with coroutines

suspend fun read(channel: AsynchronousSocketChannel, buffer: ByteBuffer): Int {
    return suspendCancellableCoroutine { cont->
        // close channel on cancellation
        cont.invokeOnCancellation {
            channel.close()
        }

        channel.read(buffer, cont, readHandler)
    }
}

suspend fun write(channel : AsynchronousSocketChannel, buffer: ByteBuffer): Int {
    return suspendCancellableCoroutine { cont->
        // close channel on cancellation
        cont.invokeOnCancellation {
            channel.close()
        }

        channel.write(buffer, cont, writeHandler)
    }
}

suspend fun accept( listener : AsynchronousServerSocketChannel):
        AsynchronousSocketChannel {
    return suspendCancellableCoroutine { cont->
        // close channel on cancellation
        cont.invokeOnCancellation {
            listener.close()
        }

        listener.accept(cont, acceptHandler)
    }
}
