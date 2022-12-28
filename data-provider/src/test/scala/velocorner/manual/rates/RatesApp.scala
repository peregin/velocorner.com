package velocorner.manual.rates

import cats.effect.{IO, IOApp}
import squants.market.{CHF, EUR, USD}
import velocorner.SecretConfig
import velocorner.feed.{ExchangeRatesFeed, HUF, HttpFeed}
import velocorner.manual.MyLocalConfig
object RatesApp extends IOApp.Simple with MyLocalConfig {

  override def run: IO[Unit] = for {
    config <- IO.delay(SecretConfig.load())
    provider <- IO(new ExchangeRatesFeed(config))
    mc <- IO.fromFuture(IO(provider.moneyContext())) // USD is the base currency
    res1 = USD(10).to(CHF)(mc) // direct conversion
    _ <- IO.println(s"direct conversion is $res1")
    res2 = EUR(10).to(HUF)(mc) // indirect conversion
    _ <- IO.println(s"indirect conversion is $res2")
    res3 = CHF(10).to(CHF)(mc) // identity conversion
    _ <- IO.println(s"identity conversion is $res3")
    _ <- IO.delay(provider.close())
    _ <- IO.delay(HttpFeed.shutdown())
  } yield ()
}
