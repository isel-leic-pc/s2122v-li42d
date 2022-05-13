package isel.leic.pc.completablefutures2;

import org.junit.Test;
import java.lang.Thread.sleep
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

public class CompletableFutureTests {

    @Test
    fun `manually completion of a completable future`() {
        val cf = CompletableFuture<Long>()

        thread {
            sleep(5000);
            cf.complete(2L)
        }
        println("get future value ...")

        println("value = ${cf.get()}")
    }
}
