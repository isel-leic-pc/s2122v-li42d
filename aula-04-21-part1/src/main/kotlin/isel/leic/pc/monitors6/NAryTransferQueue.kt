package isel.leic.pc.monitors6

import isel.leic.pc.monitors4.utils.NodeList
import isel.leic.pc.utils.await
import isel.leic.pc.utils.dueTime
import isel.leic.pc.utils.isPast
import isel.leic.pc.utils.isZero
import java.security.InvalidParameterException
import java.util.*
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * Este sincronizador é uma adaptação do enunciado
 * do teste de 1ª época de PC do 1º sem. 2020/2021 e implementa uma fila de transferência
 * de mensagens.
 * O método transfer tem a capacidade de entregar múltiplas mensagens à fila,
 * com a garantia de atomicidade nessa entrega: ou todas as mensagens são entregues
 * ou nenhuma é entregue. O método transfer é potencialmente bloqueante, retornando true
 * apenas quando todas as mensagens entregues tenham sido retiradas via o método take.
 * O método transfer pode também retornar false, no caso do tempo de espera ter expirado,
 * ou acabar com o lançamento da excepção InterruptedException em caso de interrupção.
 * Nestes dois casos, expiração de tempo e interrupção, deve ser garantido que nenhuma
 * mensagem foi removida por um take.O método take é potencialmente bloqueante,
 * retornando a mensagem removida, ou null caso não seja possível remover uma mensagem
 * dentro do tempo definido. O método take é também sensível a interrupções.
 * O sincronizador deve usar um critério FIFO (first in first out) para a finalização
 * com sucesso das operações transfer e take. Por exemplo, uma chamada a transfer
 * só deve ser concluída com sucesso quando todas as chamadas a transfer anteriores
 * tenham sido concluídas.
 */
class NAryTransferQueue<E> {
    val monitor = ReentrantLock()

    /**
     * We use kernel style on this implementation which gives in generally more simple
     * solutions. In this case we need FIFO discipline on transfer operations and
     * deliver atomicity to pending takers. These requirements are not easily done
     * without kernel style (execution delegation).
     */


    /**
     * instances of this class represents threads waiting for a transfer
     * completion
     */
    class PendingTransfer<E>(val messages: List<E>,
                            val hasTakers : Condition) {
         var done = false
    }

    /**
     * instances of this class represents threads waiting for a take
     * completion
     */
    class PendingTaker<E>(val hasValue : Condition) {
        var value : E? = null

        fun signal(msg: E) {
            value = msg
            hasValue.signal()
        }
    }

    // the lists needed for kernel style support on transfers and takers.
    // We use NodeList to have O(1) cost (and not O(n) for kotlin list implementations)
    // on remove operations
    private val pendingTransfers = NodeList<PendingTransfer<E>>()
    private val pendingTakers = NodeList<PendingTaker<E>>()

    /**
     * auxiliary method to atomically deliver the messages of a transfer
     * operation to a set of pending takers.
     * Note this assumed to run owning the monitor mutex.
     */
    private fun notifyTakers(messages: List<E>) {
        for( msg in messages) {
            val taker = pendingTakers.removeFirst().value
            taker.signal(msg)
        }
    }

    @Throws(InterruptedException::class)
    fun transfer(messages: List<E>, timeout: Duration): Boolean {
        if ( messages.size == 0)
            throw InvalidParameterException()
        monitor.withLock {
            // fast path
            // in order to do a fast path exit
            // is required that no pending transfer exists (FIFO order)
            // and there are the necessary takers (atomic delivery)
            if (pendingTransfers.isEmpty &&
              pendingTakers.size >= messages.size) {
              notifyTakers(messages)
              return true
            }
            if (timeout.isZero) return false

            // wait path
            val dueTime = timeout.dueTime()
            val node = pendingTransfers.add(
                           PendingTransfer(messages, monitor.newCondition())
            )
            do {
                try {
                    node.value.hasTakers.await(dueTime)
                    // on kernel style is required that only local state (of the method)
                    // are observed and not monitor state, unless it is guaranteed to
                    // be immutable on the time of the observation
                    if (node.value.done) return true
                    if (dueTime.isPast) {
                        pendingTransfers.remove(node)
                        return false
                    }
                }
                catch(e :InterruptedException) {
                    if (node.value.done) {
                        Thread.currentThread().interrupt()
                        return true
                    }
                    pendingTransfers.remove(node)
                    throw e
                }
            }
            while(true)
        }
    }



    @Throws(InterruptedException::class)
    fun take(timeout: Duration): E? {
       monitor.withLock {
           // the implementation of this method is similar to the transfer
           // and has been left undone has an exercise
           TODO()
       }
    }
}
