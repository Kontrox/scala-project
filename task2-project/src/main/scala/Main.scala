
object Main extends App {

  def thread(body: =>Unit): Thread = {
      val t = new Thread {
        override def run(): Unit = body
      }
      t.start()
      t
    }

  var bank = new Bank()
  var from = new Account(bank, 100000)
  var to = new Account(bank, 0)

  bank.addTransactionToQueue(from, to, 500)
  bank.addTransactionToQueue(from, to, 600)
  bank.addTransactionToQueue(from, to, 700)

  Thread.sleep(1000)
  System.out.println(from.balance.amount, to.balance.amount)


}