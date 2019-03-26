package velocorner.manual

import scala.concurrent.Awaitable
import scala.concurrent.duration._

import scala.language.postfixOps

trait AwaitSupport {

  def await[T](awaitable: Awaitable[T]): T = {
    scala.concurrent.Await.result(awaitable, 60 seconds) // 1 minute to retrieve all the activities
  }
}
