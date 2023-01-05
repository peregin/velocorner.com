package velocorner.api

import play.api.libs.json.{Format, Json}
import squants.market.{MoneyContext, NoSuchCurrencyException}
import velocorner.feed.ExchangeRatesFeed

object Money {
  implicit val moneyFormat = Format[Money](Json.reads[Money], Json.writes[Money])

  def fromSquants(m: squants.market.Money): Money = new Money(m.amount, m.currency.code)
}
case class Money(value: BigDecimal, currency: String) {

  def toSquants(implicit mc: MoneyContext): squants.market.Money =
    squants.market.Money(value, ExchangeRatesFeed.supported.getOrElse(currency, throw NoSuchCurrencyException(currency, mc)))
}
