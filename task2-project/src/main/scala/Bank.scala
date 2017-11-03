import java.util.concurrent.locks.{ReadWriteLock, ReentrantReadWriteLock}

import scala.concurrent.forkjoin.ForkJoinPool

class Bank(val allowedAttempts: Integer = 3) {

  //private val uid = ???
  private val transactionsQueue: TransactionQueue = new TransactionQueue()
  private val processedTransactions: TransactionQueue = new TransactionQueue()
  private val executorContext = new ForkJoinPool()
  private var numberOfAccounts = 0
  private var processing = false

  private val lock: ReadWriteLock = new ReentrantReadWriteLock()

  val processingThread: Thread {
    def run(): Unit
  } = new Thread {
    override def run() {
      while (true){
        if(!processing){
          processing = true
          processTransactions
        }
        Thread.sleep(200)
      }
    }
  }
  processingThread.start()

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
    transactionsQueue.iterator.foreach {
      executorContext.execute(_)
    }

    processing = false
  }

  def addAccount(initialBalance: Double): Account = {
      new Account(this, initialBalance)
  }

  def getProcessedTransactionsAsList: List[Transaction] = {
      processedTransactions.iterator.toList
  }

}
