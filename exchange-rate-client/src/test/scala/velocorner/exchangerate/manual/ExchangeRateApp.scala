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

  def apply[F[_]: Sync: Clock](cacheRef: Ref[F, Option[MoneyContext]], lastTsRef: Ref[F, Duration], expiry: Duration = 1.hour): MoneyContextProvider[F] =
    new MoneyContextProvider[F] {

      override def moneyContext(): F[MoneyContext] = for {
        maybeMc <- cacheRef.get
        lastTs <- lastTsRef.get
        now <- Clock[F].realTime
        elapsed = now - lastTs
        _ = println(s"elapsed = $elapsed, instance = $maybeMc, last = $lastTs")
        mc <- maybeMc match {
          case _ if elapsed > expiry => createMoneyContext(now)
          case None => createMoneyContext(now)
          case Some(m) => m.pure[F]
        }
      } yield mc

      private def createMoneyContext(now: Duration): F[MoneyContext] = for {
        m <- defaultMoneyContext.pure[F]
        _ = println("creating MoneyContext")
        _ <- cacheRef.set(m.some)
        _ <- lastTsRef.set(now)
      } yield m
    }
}

object ExchangeRateApp extends IOApp.Simple {

  override def run: IO[Unit] = for {
    lastTsRef <- Ref[IO].of[Duration](0.hour)
    cacheRef <- Ref[IO].of[Option[MoneyContext]](none)
    rmc = RefreshingMoneyContextProvider[IO](cacheRef = cacheRef, lastTsRef = lastTsRef, expiry = 2.second)
    _ <- rmc.moneyContext()
    _ <- IO.sleep(1.second)
    _ <- IO.println("test")
    _ <- rmc.moneyContext()
    _ <- IO.sleep(2.second)
    _ <- rmc.moneyContext()
  } yield ()
}
