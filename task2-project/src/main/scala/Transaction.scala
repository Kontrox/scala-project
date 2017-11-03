import exceptions._
import scala.collection.mutable

object TransactionStatus extends Enumeration {
  val SUCCESS, PENDING, FAILED = Value
}

class TransactionQueue {

  var queue: List[Transaction] = List()
  // Remove and return the first element from the queue
  def pop: Transaction = {
    val firstEl = queue.head
    queue = queue.tail
    firstEl
  }

  // Return whether the queue is empty
  def isEmpty: Boolean = {
    queue.isEmpty
  }

  // Add new element to the back of the queue
  def push(t: Transaction): Unit = {
    queue = queue ::: List(t)
  }

  // Return the first element from the queue without removing it
  def peek: Transaction = {
    queue.head
  }

  // Return an iterator to allow you to iterate over the queue
  def iterator: Iterator[Transaction] = {
    queue.toIterator
  }

}

class Transaction(val transactionsQueue: TransactionQueue,
                  val processedTransactions: TransactionQueue,
                  val from: Account,
                  val to: Account,
                  val amount: Double,
                  val allowedAttemps: Int,
                  var attempts: Int = 0) extends Runnable {

  var status: TransactionStatus.Value = TransactionStatus.PENDING

  override def run(): Unit = {

    def doTransaction(): Unit = {
      try{
        attempts += 1
        from withdraw amount
        to deposit amount
        status = TransactionStatus.SUCCESS
        processedTransactions.push(this)
      } catch {
        case c: IllegalAmountException =>
          status = TransactionStatus.FAILED
          if(attempts < allowedAttemps) transactionsQueue.push(this)
          else processedTransactions.push(this)
        case c: NoSufficientFundsException =>
          status = TransactionStatus.FAILED
          if(attempts < allowedAttemps) transactionsQueue.push(this)
          else processedTransactions.push(this)
      }
    }

    if (from.uid < to.uid) from synchronized {
      to synchronized {
        doTransaction()
      }
    } else to synchronized {
      from synchronized {
        doTransaction()
      }
    }

    // Extend this method to satisfy new requirements.


  }
}
