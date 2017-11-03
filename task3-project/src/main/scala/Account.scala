import java.util.concurrent.locks.{ReadWriteLock, ReentrantReadWriteLock}

import akka.actor._
import exceptions._

import scala.collection.immutable.HashMap

case class TransactionRequest(toAccountNumber: String, amount: Double)

case class TransactionRequestReceipt(toAccountNumber: String,
                                     transactionId: String,
                                     transaction: Transaction)

case class BalanceRequest()

class Account(val accountId: String, val bankId: String, val initialBalance: Double = 0) extends Actor {

  private var transactions = HashMap[String, Transaction]()

  class Balance(var amount: Double) {}

  val balance = new Balance(initialBalance)
  private val lock: ReadWriteLock = new ReentrantReadWriteLock()

  def getFullAddress: String = {
    bankId + accountId
  }

  def getTransactions: List[Transaction] = {
    transactions.values.toList
    // Should return a list of all Transaction-objects stored in transactions
  }

  def allTransactionsCompleted: Boolean = {
    val list = getTransactions
    var completed = true
    list.foreach((i: Transaction) =>
      if(!i.isCompleted) completed = false
    )
    completed
    // Should return whether all Transaction-objects in transactions are completed
  }

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

  def sendTransactionToBank(t: Transaction): Unit = {
    // Should send a message containing t to the bank of this account
    val bank = BankManager.findBank(bankId)
    bank.tell(t, sender)
  }

  def sendTransactionReceiptToBank(tr: TransactionRequestReceipt): Unit = {
    val bank = BankManager.findBank(bankId)
    bank.tell(tr, sender)
  }

  def transferTo(accountNumber: String, amount: Double): Transaction = {

    val t = new Transaction(from = getFullAddress, to = accountNumber, amount = amount)

    if (reserveTransaction(t)) {
      try {
        withdraw(amount)
        sendTransactionToBank(t)
      } catch {
        case _: NoSufficientFundsException | _: IllegalAmountException =>
          t.status = TransactionStatus.FAILED
      }
    }

    t

  }

  def reserveTransaction(t: Transaction): Boolean = {
    if (!transactions.contains(t.id)) {
      transactions += (t.id -> t)
      return true
    }
    false
  }

  /*
  When the receiving Account (B) has received t, B should process t, and send a TransactionRequestReceipt,
  saying that t succeeded, back to the A, the same way t was sent (only backwards).

  If the transaction somehow fails on the way (e.g., if a Bank or Account does not exist), a TransactionRequestReceipt
  saying that t failed should be sent back to A from the point of failure.

  When A has received a TransactionRequestReceipt, it should update the information about t in
  the HashMap that the transaction was stored in earlier.
   */

  override def receive = {
    case IdentifyActor => sender ! this

    case TransactionRequestReceipt(to, transactionId, transaction) => {
      val obj = transactions.get(transactionId)
      obj.get.receiptReceived = true
      if(transaction.status == TransactionStatus.FAILED){
        deposit(transaction.amount)
      }
    }

    case BalanceRequest => {sender ! balance.amount} // Should return current balance

    case t: Transaction => {
      deposit(t.amount)
      if(t.status != TransactionStatus.FAILED){
        t.status = TransactionStatus.SUCCESS
      }
      val receipt = TransactionRequestReceipt(t.from, t.id, t)
      sendTransactionReceiptToBank(receipt)
      // Handle incoming transaction
    }

    case msg => {
      System.out.println(msg)
    }
  }


}
