package velocorner.crawler

import io.circe._
import io.circe.parser._
import org.scalatest.Assertions.fail
import velocorner.util.CloseableResource

import scala.io.Source

// with debugging information
trait DecodeResource extends CloseableResource {

  def load(resource: String): String = withCloseable(Source.fromURL(getClass.getResource(resource)))(_.mkString)

  def assert[T: Decoder](resource: String): T = {
    val reply = load(resource)
    parse(reply) match {
      case Left(f)      => fail(f.message)
      case Right(value) =>
        println("parsing succeeded")
        value.as[T] match {
          case Left(err)          => fail("failed to decode " + err.reason.toString + " on json " + value)
          case Right(suggestions) => suggestions
        }
    }
  }
}
