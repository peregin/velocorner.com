package velocorner.manual

import scala.concurrent.Awaitable
import scala.concurrent.duration._

import scala.language.postfixOps

trait AwaitSupport {

  def awaitOn[T](awaitable: Awaitable[T]): T =
    scala.concurrent.Await.result(awaitable, 120 seconds) // 2 minutes to retrieve all the activities
}
