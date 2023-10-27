package velocorner.api

import play.api.libs.json.{Format, Json}

object Ping {
  implicit val format: Format[Ping] = Format[Ping](Json.reads[Ping], Json.writes[Ping])
}

case class Ping(message: String)
