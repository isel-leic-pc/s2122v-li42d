package isel.leic.pc.utils

class NodeList<T> : Iterable<T> {

    class Node<T> {
        internal var value : T?
        internal lateinit var next : Node<T>
        internal lateinit var previous : Node<T>

        companion object {
            fun <T> insertBefore(_value : T, _node : Node<T> ) : Node<T> {
                return Node<T>(_value, _node, before = true)
            }

            fun <T> insertAfter(_value : T, _node : Node<T> ) : Node<T> {
                return Node<T>(_value, _node, before = false)
            }
        }

        private inline fun insertBefore( _node : Node<T> ) {
            previous = _node.previous
            previous.next = this

            _node.previous = this
            next = _node;
        }

        private inline fun insertAfter( _node : Node<T> ) {
            next = _node.next
            next.previous = this
            _node.next = this
            previous = _node;
        }


        private constructor(_value : T, _node : Node<T>, before: Boolean) {
            value = _value
            if (before) insertBefore(_node)
            else  insertAfter(_node)
        }

        internal constructor( ) {
            value = null
            next = this
            previous = this;
        }

        internal fun remove() {
            previous.next = next
            next.previous = previous
        }
    }

    private var head = Node<T>()
    private var count = 0

    val size get() = count

    val isEmpty get() = count == 0

    fun add(value : T)  : Node<T> {
        count++
        return Node.insertBefore(value, head)
    }

    fun addFirst(value : T) : Node<T> {
        count++
        return Node.insertAfter(value, head)
    }

    fun removeFirst() : Node<T> {
        if (isEmpty)
            throw IllegalStateException()
        val node =  head.next
        node.remove()
        return node
    }


    fun remove(node : Node<T> ) {
        if (isEmpty)
            throw IllegalStateException()
        node.remove()
    }

    val first
        get() : T {
            if (isEmpty)
                throw IllegalStateException()
            return head.next.value!!
        }

    fun last() : T {
        if (isEmpty)
            throw IllegalStateException()
        return head.previous.value!!
    }

    override fun iterator(): Iterator<T> {
        return ListIterator()
    }

    internal fun show() {
        forEach {
            println(it)
        }
        println()
    }

    private inner class ListIterator :Iterator<T> {
        var current = head.next

        override fun hasNext(): Boolean {
            return  (current  != head)
        }

        override fun next(): T {
            if (!hasNext()) throw IllegalStateException()
            val currVal = current.value!!
            current = current.next
            return currVal
        }
    }

}