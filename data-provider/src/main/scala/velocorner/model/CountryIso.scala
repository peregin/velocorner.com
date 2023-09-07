package velocorner.model

import play.api.libs.json.{Format, Json}
import velocorner.util.JsonIo

case class CountryIso(name: String, code: String)

object CountryIso {

  implicit val countryFormat: Format[CountryIso] = Format[CountryIso](Json.reads[CountryIso], Json.writes[CountryIso])
}
