import java.util.concurrent.locks.{ReadWriteLock, ReentrantReadWriteLock}

import exceptions.{IllegalAmountException, NoSufficientFundsException}

object Bank {

    private var idCounter: Int = 0
    private val lock: ReadWriteLock = new ReentrantReadWriteLock()

    def transaction(from: Account, to: Account, amount: Double): Unit = { // Implement
        /*if(amount < 0){
            throw new IllegalAmountException("Invalid transfer. Negative amount.")
        }
        else if(from.getBalanceAmount - amount < 0){
            throw new NoSufficientFundsException("Invalid transfer. Insufficient funds.")
        }*/
        from.withdraw(amount)
        to.deposit(amount)
    }

    def getUniqueId: Int = {
        lock.writeLock().lock()
        try {
            idCounter += 1
            idCounter
        } finally { lock.writeLock().unlock() }
    }

}
