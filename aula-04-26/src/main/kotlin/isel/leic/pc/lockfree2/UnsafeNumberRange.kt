package isel.leic.pc.lockfree2


import java.util.concurrent.atomic.AtomicReference

// Must keep the invariant lower <= upper!
class UnsafeNumberRange(private var lower: Int, private var upper:Int) {

    private class Range(val lower: Int, val upper:Int)

    private val range = AtomicReference(Range(lower, upper))

    init {
        if (lower > upper)
            throw  IllegalArgumentException();
    }


    fun setLower(l : Int) {
        while(true) {
            val obsRange = range.get()
            if (l > obsRange.upper)
                throw  IllegalArgumentException();
            if (range.compareAndSet(obsRange, Range(l, obsRange.upper)))
                return;
        }

    }

    fun setUpper(u : Int) {
        if (u < lower)
            throw  IllegalArgumentException();
        upper = u;
    }
}