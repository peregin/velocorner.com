package velocorner.api.brand

import play.api.libs.json.{Format, Json}
import velocorner.api.Money

object ProductDetails {
  implicit val pdFormat = Format[ProductDetails](Json.reads[ProductDetails], Json.writes[ProductDetails])
}

case class ProductDetails(
    market: Marketplace,
    brand: Option[Brand],
    name: String,
    description: Option[String],
    price: Money,
    imageUrl: String,
    productUrl: String,
    reviewStars: Int
)
