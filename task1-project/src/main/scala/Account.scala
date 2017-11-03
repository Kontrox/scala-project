import java.util.concurrent.locks.{ReadWriteLock, ReentrantReadWriteLock}
import exceptions._

class Account(initialBalance: Double, val uid: Int = Bank.getUniqueId) {

    private var balance: Double = initialBalance
    private val lock: ReadWriteLock = new ReentrantReadWriteLock()

    def withdraw(amount: Double): Unit = {  // Implement
        lock.writeLock().lock()
        try {
            if(amount < 0){
                throw new IllegalAmountException("Invalid withdraw. Negative amount.")
            }else if(amount > balance){
                throw new NoSufficientFundsException("Invalid withdraw. Insufficient funds.")
            }
            balance -= amount
        } finally { lock.writeLock().unlock() }
    }

    def deposit(amount: Double): Unit = { // Implement
        lock.writeLock().lock()
        try {
            if(amount < 0){
                throw new IllegalAmountException("Invalid deposit. Negative amount.")
            }
            balance += amount
        } finally { lock.writeLock().unlock() }
    }

    def getBalanceAmount: Double = { // Implement
        balance
    }
}
