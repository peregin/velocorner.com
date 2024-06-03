package velocorner.feed

import com.typesafe.scalalogging.LazyLogging
import squants.market._
import velocorner.SecretConfig
import play.api.libs.json._
import velocorner.feed.ExchangeRatesFeed.supported

import scala.concurrent.Future

trait RatesFeed {

  def moneyContext(): Future[MoneyContext]
}

object HUF extends Currency("HUF", "Hungarian Forint", "Ft", 2)

object ExchangeRatesFeed {
  // supported by both, exchange rates services and squants
  val supported = Map[String, Currency](
    "ARS" -> ARS,
    "AUD" -> AUD,
    "BRL" -> BRL,
    "BTC" -> BTC,
    "CAD" -> CAD,
    "CHF" -> CHF,
    "CLP" -> CLP,
    "CNY" -> CNY,
    "CZK" -> CZK,
    "DKK" -> DKK,
    "EUR" -> EUR,
    "GBP" -> GBP,
    "HKD" -> HKD,
    "HUF" -> HUF,
    "INR" -> INR,
    "JPY" -> JPY,
    "KRW" -> KRW,
    "MXN" -> MXN,
    "MYR" -> MYR,
    "NAD" -> NAD,
    "NOK" -> NOK,
    "NZD" -> NZD,
    "RUB" -> RUB,
    "SEK" -> SEK,
    "TRY" -> TRY,
    "USD" -> USD,
    "XAG" -> XAG,
    "XAU" -> XAU,
    "ZAR" -> ZAR
  )
}

class ExchangeRatesFeed(override val config: SecretConfig) extends HttpFeed with LazyLogging with RatesFeed {

  lazy val baseUrl = config.getRatesUrl

  override def moneyContext(): Future[MoneyContext] = moneyContext(USD)

  def moneyContext(base: Currency): Future[MoneyContext] =
    ws(_.url(s"$baseUrl/api/rates/${base.code}").get()).map(resp => Json.parse(resp.body)).map { json =>
      val baseUsed = (json \ "base").as[String]
      require(baseUsed == base.code, s"base currency must be $base, not $baseUsed")
      val rates = (json \ "rates").as[Map[String, Double]]
      MoneyContext(
        USD,
        supported.values.toSet + base,
        rates
          .map { case (code, rate) => (supported.get(code), rate) }
          .collect { case (Some(currency), rate) if currency != base => CurrencyExchangeRate(USD(1), currency(rate)) }
          .toSeq
      )
    }
}
