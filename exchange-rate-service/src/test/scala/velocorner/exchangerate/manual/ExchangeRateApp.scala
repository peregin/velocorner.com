package velocorner.exchangerate.manual

import cats.implicits._
import cats.effect.kernel.{Ref, Sync}
import cats.effect.{Clock, IO, IOApp}
import squants.market.{defaultMoneyContext, MoneyContext, USD}

import scala.concurrent.duration.{Duration, DurationInt}

// implementation should provide various ways to retrieve a MoneyContext:
// - cached one
// - refreshed with some regular frequency
// - etc
trait MoneyContextProvider[F[_]] {

  def moneyContext(): F[MoneyContext]
}

object RefreshingMoneyContextProvider {

  def apply[F[_]: Sync: Clock](expiry: Duration = 1.hour): MoneyContextProvider[F] = new MoneyContextProvider[F] {

    val cache = Ref[F].of[Option[MoneyContext]](None)
    val lastTs = Ref[F].of[Duration](0.hour)

    override def moneyContext(): F[MoneyContext] = for {
      cacheRef <- cache
      lastTsRef <- lastTs
      maybeMc <- cacheRef.get
      last <- lastTsRef.get
      now <- Clock[F].realTime
      elapsed = now - last
      _ = println(s"elapsed = $elapsed")
      mc <- maybeMc match {
        case _ if elapsed > expiry =>
          for {
            m <- createMoneyContext()
            _ <- cacheRef.set(m.some)
            _ <- lastTsRef.set(now)
          } yield m
        case Some(m) =>
          m.pure[F]
      }
    } yield mc

    private def createMoneyContext(): F[MoneyContext] =
      {
        println("creating MoneyContext")
        defaultMoneyContext
      }.pure[F]
  }
}

object ExchangeRateApp extends IOApp.Simple {

  val rmc = RefreshingMoneyContextProvider[IO](expiry = 10.second)
  override def run: IO[Unit] = for {
    _ <- rmc.moneyContext()
    _ <- IO.sleep(1.second)
    _ <- IO.println("test")
    _ <- rmc.moneyContext()
    _ <- IO.sleep(1.second)
    _ <- rmc.moneyContext()
  } yield ()
}
