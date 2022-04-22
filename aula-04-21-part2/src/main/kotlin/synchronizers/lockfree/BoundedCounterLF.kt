package synchronizers.lockfree

import java.util.concurrent.atomic.AtomicInteger

/**
 * this is an (incorrect) implementation of the bounded counter
 * since it not prevents that the counter exceed its limits (why?)
 */
class BoundedCounterLF(val min: Int, val max: Int ) {
    var value = AtomicInteger(min)

    fun inc() : Boolean {
        if (value.get() == max) return false
        value.incrementAndGet()
        return true
    }

    fun dec() : Boolean {
        if (value.get() == min) return false
        value.decrementAndGet()
        return true
    }
}

/**
 * this is an (correct) implementation of the bounded counter (using CAS)
 * that prevents that the counter does not exceed its limits
 */
class BoundedCounter2LF(val min: Int, val max: Int ) {
    var value = AtomicInteger(min)

    fun inc() : Boolean {
         do {
             val obsValue = value.get()
             if (obsValue == max) return false
             if (value.compareAndSet(obsValue, obsValue + 1))
                 return true
         }
         while(true)
    }

    fun dec() : Boolean {
        do {
            val obsValue = value.get()
            if (obsValue == min) return false
            if (value.compareAndSet(obsValue, obsValue - 1))
                return true
        }
        while(true)
    }
}