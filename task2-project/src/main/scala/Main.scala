
object Main extends App {

  def thread(body: =>Unit): Thread = {
      val t = new Thread {
        override def run(): Unit = body
      }
      t.start()
      t
    }

  var bank = new Bank()
  var queue = new TransactionQueue()
  var processedTransQueue = new TransactionQueue()
  var from = new Account(bank, 100000)
  var to = new Account(bank, 0)

  queue.push(new Transaction(queue, processedTransQueue, from, to, 500, 1))
  System.out.println(queue.queue, from.balance.amount, to.balance.amount)
  queue.push(new Transaction(queue, processedTransQueue, from, to, 600, 1))
  System.out.println(queue.queue, from.balance.amount, to.balance.amount)
  queue.push(new Transaction(queue, processedTransQueue, from, to, 700, 1))
  System.out.println(queue.queue, from.balance.amount, to.balance.amount)

  Thread.sleep(1000)
  System.out.println(queue.queue, from.balance.amount, to.balance.amount)


}