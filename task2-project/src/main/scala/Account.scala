import java.util.concurrent.locks.{ReadWriteLock, ReentrantReadWriteLock}

import exceptions._

class Account(val bank: Bank, initialBalance: Double) {

  class Balance(var amount: Double) {}

  val balance = new Balance(initialBalance)
  private val uid: Int = bank.generateAccountId
  private val lock: ReadWriteLock = new ReentrantReadWriteLock()

  def withdraw(amount: Double): Unit = {  // Implement
    lock.writeLock().lock()
    try {
      if(amount < 0){
        throw new IllegalAmountException("Invalid withdraw. Negative amount.")
      } else if(balance.amount - amount < 0){
        throw new NoSufficientFundsException("Invalid withdraw. Insufficient funds.")
      }
      balance.amount -= amount
    } finally { lock.writeLock().unlock() }
  }

  def deposit(amount: Double): Unit = { // Implement
    lock.writeLock().lock()
    try {
      if(amount < 0){
        throw new IllegalAmountException("Invalid deposit. Negative amount.")
      }
      balance.amount += amount
    } finally { lock.writeLock().unlock() }
  }

  def getBalanceAmount: Double = {
    lock.writeLock().lock()
    try balance.amount
    finally { lock.writeLock().unlock() }
  }

  def getAccountId: Int = { uid }

  def transferTo(account: Account, amount: Double): Unit = {
    bank addTransactionToQueue (this, account, amount)
  }


}
