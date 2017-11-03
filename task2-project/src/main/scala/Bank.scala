import java.util.concurrent.locks.{ReadWriteLock, ReentrantReadWriteLock}

import scala.concurrent.forkjoin.ForkJoinPool

class Bank(val allowedAttempts: Integer = 3) {

  //private val uid = ???
  private val transactionsQueue: TransactionQueue = new TransactionQueue()
  private val processedTransactions: TransactionQueue = new TransactionQueue()
  private val executorContext = new ForkJoinPool()
  private var numberOfAccounts = 0
  private var processing = false

  private val generationLock: ReadWriteLock = new ReentrantReadWriteLock()
  private val transactionLock: ReadWriteLock = new ReentrantReadWriteLock()

  val processingThread: Thread {
    def run(): Unit
  } = new Thread {
    override def run() {
      while (true){
        if(!processing){
          processing = true
          transactionLock.writeLock().lock()
          processTransactions()
        }
        Thread.sleep(200)
      }
    }
  }
  processingThread.start()

  def addTransactionToQueue(from: Account, to: Account, amount: Double): Unit = {
    transactionLock.writeLock().lock()
    try{
      transactionsQueue push new Transaction(
        transactionsQueue, processedTransactions, from, to, amount, allowedAttempts)
    } finally { transactionLock.writeLock().unlock() }

  }

  def generateAccountId: Int = {
      generationLock.writeLock().lock()
      try{
        numberOfAccounts += 1
        numberOfAccounts
      } finally {generationLock.writeLock().unlock()}
  }

  private def processTransactions(): Unit = {
    /*val list = transactionsQueue.iterator
    System.out.println(list)
    while (list.hasNext){
      val it = list.next()
      executorContext.execute(it)
    }*/
    while (!transactionsQueue.isEmpty){
      val trans = transactionsQueue.pop
      executorContext.execute(trans)
      //processedTransactions.push(trans)
    }
    transactionLock.writeLock().unlock()
    processing = false
  }

  def addAccount(initialBalance: Double): Account = {
      new Account(this, initialBalance)
  }

  def getProcessedTransactionsAsList: List[Transaction] = {
      processedTransactions.iterator.toList
  }

}
