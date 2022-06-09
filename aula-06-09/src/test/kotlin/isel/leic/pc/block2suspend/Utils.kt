package isel.leic.pc.block2suspend

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.channels.AsynchronousSocketChannel



suspend fun close(channel: AsynchronousSocketChannel) {
        withContext(Dispatchers.IO) {
                channel.close()
        }
}