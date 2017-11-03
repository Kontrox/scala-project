import java.util.NoSuchElementException

import akka.actor._
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration._
import akka.util.Timeout

case class GetAccountRequest(accountId: String)

case class CreateAccountRequest(initialBalance: Double)

case class IdentifyActor()

class Bank(val bankId: String) extends Actor {

  val accountCounter = new AtomicInteger(1000)

  def createAccount(initialBalance: Double): ActorRef = {
    val accId = accountCounter.incrementAndGet()
    BankManager.createAccount(accId.toString, this.bankId, initialBalance)
    // Should create a new Account Actor and return its actor reference. Accounts should be assigned with unique ids (increment with 1).
  }

  def findAccount(accountId: String): Option[ActorRef] = {
    try{
      Some(BankManager.findAccount(this.bankId, accountId))
    }catch{
      case c: NoSuchElementException => None
    }
    // Use BankManager to look up an account with ID accountId
  }

  def findOtherBank(bankId: String): Option[ActorRef] = {
    try{
      Some(BankManager.findBank(bankId))
    }catch{
      case c: NoSuchElementException => None
    }

    /*
    def toInt(in: String): Option[Int] = {
    try {
        Some(Integer.parseInt(in.trim))
    } catch {
        case e: NumberFormatException => None
    }
}
     */
    // Use BankManager to look up a different bank with ID bankId
  }

  override def receive = {
    case CreateAccountRequest(initialBalance) => sender ! createAccount(initialBalance) // Create a new account
    case GetAccountRequest(id) => sender ! findAccount(id) // Return account
    case IdentifyActor => sender ! this
    case t: Transaction => processTransaction(t)
    case tr: TransactionRequestReceipt => processReceipt(tr)
    case msg => ???
  }

  def processReceipt(receipt: TransactionRequestReceipt): Unit = {
    implicit val timeout = new Timeout(5 seconds)
    val t = receipt.transaction
    val isInternal = receipt.toAccountNumber.length <= 4
    val toBankId = if (isInternal) bankId else receipt.toAccountNumber.substring(0, 4)
    val toAccountId = if (isInternal) receipt.toAccountNumber else receipt.toAccountNumber.substring(4)

    if(toBankId == this.bankId){
      val acc = findAccount(toAccountId)
      acc.get.tell(receipt, sender)
    }else{
      val bank = findOtherBank(toBankId)
      bank.get.tell(receipt, sender)
    }
  }

  def processTransaction(t: Transaction): Unit = {
    implicit val timeout = new Timeout(5 seconds)
    val isInternal = t.to.length <= 4
    val toBankId = if (isInternal) bankId else t.to.substring(0, 4)
    val toAccountId = if (isInternal) t.to else t.to.substring(4)
    val transactionStatus = t.status


    // This method should forward Transaction t to an account or another bank, depending on the "to"-address.
    // HINT: Make use of the variables that have been defined above.
    if(toBankId == this.bankId){
      val acc = findAccount(toAccountId)
      if(acc.isDefined){
        acc.get.tell(t, sender)
      }else{
        t.status = TransactionStatus.FAILED
        val acc = findAccount(if(t.from.length <= 4) t.from else t.from.substring(4))
        val receipt = TransactionRequestReceipt(t.from, t.id, t)
        acc.get.tell(receipt, sender)
      }
    }else{
      val bank = findOtherBank(toBankId)
      if(bank.isDefined){
        bank.get.tell(t, sender)
      }else{
        t.status = TransactionStatus.FAILED
        val acc = findAccount(if(t.from.length <= 4) t.from else t.from.substring(4))
        val receipt = TransactionRequestReceipt(t.from, t.id, t)
        acc.get.tell(receipt, sender)
      }
    }
  }
}