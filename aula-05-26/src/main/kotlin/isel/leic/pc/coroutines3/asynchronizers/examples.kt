package isel.leic.pc.coroutines3.asynchronizers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


suspend fun action1() {
    delay(10)
}

suspend fun action2() {
    delay(10)
}

@Volatile
private var insideCritical = false
private val mutex = Mutex()


suspend fun atomicActions() {
    mutex.withLock {
        if (insideCritical)
            throw IllegalStateException("reentrancy error!")
        insideCritical = true
        action1()
        action2()
        insideCritical = false
    }
}


fun main() {
    runBlocking {
        repeat(1000) {
            launch(Dispatchers.Default) {
                atomicActions()
            }
        }
    }
}
