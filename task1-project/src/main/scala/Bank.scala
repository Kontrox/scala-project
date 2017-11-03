import java.util.concurrent.locks.{ReadWriteLock, ReentrantReadWriteLock}

object Bank {

    private var idCounter: Int = 0
    private val lock: ReadWriteLock = new ReentrantReadWriteLock()

    def transaction(from: Account, to: Account, amount: Double): Unit = { // Implement
        from withdraw amount
        to deposit amount
    }

    def getUniqueId: Int = {
        lock.writeLock().lock()
        try {
            idCounter += 1
            idCounter
        } finally { lock.writeLock().unlock() }
    }

}
