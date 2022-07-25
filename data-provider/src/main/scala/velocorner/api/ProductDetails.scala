package velocorner.api

import play.api.libs.json.{Format, Json}
import velocorner.model.Money
import velocorner.model.brand.{Brand, Marketplace}

object ProductDetails {
  implicit val pdFormat = Format[ProductDetails](Json.reads[ProductDetails], Json.writes[ProductDetails])
}

case class ProductDetails(
    market: Marketplace,
    brand: Option[Brand],
    name: String,
    description: Option[String],
    price: Money,
    url: String
)
