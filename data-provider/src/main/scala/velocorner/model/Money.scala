package velocorner.model

import play.api.libs.json.{Format, Json}

object Money {
  implicit val moneyFormat = Format[Money](Json.reads[Money], Json.writes[Money])
}
case class Money(value: BigDecimal, currency: String)
