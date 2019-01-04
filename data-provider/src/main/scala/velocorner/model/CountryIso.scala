package velocorner.model

import play.api.libs.json.{Format, Json}
import velocorner.util.JsonIo

case class CountryIso(name: String, code: String)

object CountryIso {

  implicit val clubFormat = Format[CountryIso](Json.reads[CountryIso], Json.writes[CountryIso])

  def fromResources(): Map[String, String] = {
    val countries = JsonIo.readReadFromResource[List[CountryIso]]("/countries.json")
    countries.map(ci => (ci.name.toLowerCase, ci.code)).toMap
  }
}
