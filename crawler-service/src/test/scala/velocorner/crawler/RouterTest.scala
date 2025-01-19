package velocorner.crawler

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.http4s.Status
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.dsl.io._
import org.http4s.dsl.io.GET
import org.http4s.implicits._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import velocorner.api.brand.ProductDetails
import velocorner.crawler.model.codec

class RouterTest extends AsyncFlatSpec with AsyncIOSpec with should.Matchers {

  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  "router" should "handle valid search term" in
    (for {
      rsp <- new Router[IO](Nil).routes.orNotFound.run(GET(uri"/search/SRAM"))
      res <- rsp.as[List[ProductDetails]]
    } yield res).asserting { res =>
      res shouldBe empty
    }

  "router" should "handle empty search term" in
    (for {
      rsp <- new Router[IO](Nil).routes.orNotFound.run(GET(uri"/search/"))
      res <- rsp.as[String]
    } yield (rsp, res)).asserting { case (rsp, res) =>
      rsp.status shouldBe Status.BadRequest
      res shouldBe "empty search term"
    }
}
