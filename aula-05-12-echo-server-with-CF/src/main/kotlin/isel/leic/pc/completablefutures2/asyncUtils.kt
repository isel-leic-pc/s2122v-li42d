package isel.leic.pc.completablefutures2

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.CompletableFuture

val completion = object : CompletionHandler<Int, CompletableFuture<Int>> {
    override fun completed(result: Int?, attachment: CompletableFuture<Int>) {
        attachment.complete(result)
    }

    override fun failed(exc: Throwable?, attachment: CompletableFuture<Int>) {
        attachment.completeExceptionally(exc)
    }

}

val completionSocket = object : CompletionHandler<AsynchronousSocketChannel,
        CompletableFuture<AsynchronousSocketChannel>> {
    override fun completed(result: AsynchronousSocketChannel,
                           attachment: CompletableFuture<AsynchronousSocketChannel>) {
        attachment.complete(result)
    }

    override fun failed(exc: Throwable?,
                        attachment: CompletableFuture<AsynchronousSocketChannel>) {
        attachment.completeExceptionally(exc)
    }

}


fun AsynchronousSocketChannel.readAsync(buffer : ByteBuffer)
            : CompletableFuture<Int> {
    val cf = CompletableFuture<Int>()

    read(buffer, cf, completion)
    return cf
}

fun AsynchronousSocketChannel.writeAsync(buffer : ByteBuffer) :
        CompletableFuture<Int> {
    val cf = CompletableFuture<Int>()

    write(buffer, cf, completion)
    return cf
}

fun AsynchronousServerSocketChannel.acceptAsync() :
        CompletableFuture<AsynchronousSocketChannel> {
    val cf = CompletableFuture<AsynchronousSocketChannel>()

    accept(cf, completionSocket)
    return cf
}