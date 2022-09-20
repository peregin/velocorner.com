package velocorner.crawler

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import velocorner.api.Money
import velocorner.api.brand.{Brand, Marketplace, ProductDetails}

object model {

  // models are defined in the data-provider module
  // setup codecs to encode in json with circe
  implicit val codecMarket: Codec[Marketplace] = deriveCodec
  implicit val codecMoney: Codec[Money] = deriveCodec
  implicit val codecBrand: Codec[Brand] = deriveCodec
  implicit val codec: Codec[ProductDetails] = deriveCodec
}
