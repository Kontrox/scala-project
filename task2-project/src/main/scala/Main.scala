
object Main extends App {

  def thread(body: =>Unit): Thread = {
      val t = new Thread {
        override def run(): Unit = body
      }
      t.start()
      t
    }
  var queue = new TransactionQueue()
  System.out.println("empty: "+queue.isEmpty)
  queue.push(new Transaction(null, null, null, null, 500, 1))
  System.out.println(queue.queue)
  System.out.println("empty: "+queue.isEmpty)
  queue.push(new Transaction(null, null, null, null, 600, 1))
  System.out.println(queue.queue)
  queue.push(new Transaction(null, null, null, null, 700, 1))
  System.out.println(queue.queue)
  System.out.println(queue.pop)
  System.out.println("peek: "+queue.peek)
  System.out.println(queue.queue)
}