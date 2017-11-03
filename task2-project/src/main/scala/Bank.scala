import java.util.concurrent.locks.{ReadWriteLock, ReentrantReadWriteLock}

import scala.concurrent.forkjoin.ForkJoinPool

class Bank(val allowedAttempts: Integer = 3) {

  private val uid = ???
  private val transactionsQueue: TransactionQueue = new TransactionQueue()
  private val processedTransactions: TransactionQueue = new TransactionQueue()
  private val executorContext = ???
  private var numberOfAccounts = 0

  private val lock: ReadWriteLock = new ReentrantReadWriteLock()

  def addTransactionToQueue(from: Account, to: Account, amount: Double): Unit = {
      transactionsQueue push new Transaction(
      transactionsQueue, processedTransactions, from, to, amount, allowedAttempts)
  }

  def generateAccountId: Int = {
      lock.writeLock().lock()
      try{
        numberOfAccounts += 1
        numberOfAccounts
      } finally {lock.writeLock().unlock()}
  }

  private def processTransactions: Unit = {

  }

  def addAccount(initialBalance: Double): Account = {
      new Account(this, initialBalance)
  }

  def getProcessedTransactionsAsList: List[Transaction] = {
      processedTransactions.iterator.toList
  }

}
