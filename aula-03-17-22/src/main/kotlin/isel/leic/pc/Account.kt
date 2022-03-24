package isel.leic.pc


class Account(private var balance : Long = 0L) {
    companion object {
        val globalMutex = Any()
    }

    /**
     * a non thread safe tranfer operation
     * since the access to shared balances are not synchronized
     */
    fun transfer0(dst: Account, value : Long) : Boolean {

        if (balance < value)
            return false
        else {
            balance -= value
            dst.balance += value
        }
        return true
    }

    /**
     * A thread safe transfer operation, but with terrible performance
     * since there is a single mutex shared by all accounts, what means that just
     * one tranfer can be done at any time between arbitrary accounts
     */
    fun transfer(dst: Account, value : Long) : Boolean {
        synchronized(globalMutex) {
            if (balance < value)
                return false
            else {

                balance -= value
                dst.balance += value
            }
        }
        return true
    }

    /**
     * This version seems to have better performance, since
     * other accounts transfers can de done concurrently,
     * but deadlock can happen.
     * Consider a transfer A -> B and at the same time a transfer B -> A.
     * if first transfer acquires A mutex and second transfer acquires B mutex,
     * a deadlock happens, that is, the operation is eternally blocked
     */
    fun transfer1(dst: Account, value : Long) : Boolean {

        synchronized(this) {
            if (balance < value)
                return false
            else {
                synchronized(dst) {
                    balance -= value
                    dst.balance += value
                }
            }
        }
        return true
    }

    /**
     * An implementation immune to deadlock, since all mutex accounts
     * are acquired by a single global order
     */
    fun transfer2(dst: Account, value : Long) : Boolean {
        val (mutex1, mutex2) =
            if (hashCode() < dst.hashCode())
                Pair(this, dst)
            else
                Pair(dst, this)

        synchronized(mutex1) {
            if (balance < value)
                return false
            else {
                synchronized(mutex2) {
                    balance -= value
                    dst.balance += value
                }
            }
        }
        return true
    }

    /**
     * this innocent method is not really thread safe
     * We will talk about this later in the course
     */
    fun getBalance() : Long {
        return balance
    }
}