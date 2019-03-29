package velocorner.model

import play.api.libs.json.{Format, Json}


object Counter {
  implicit val entryFormat = Format[Counter](Json.reads[Counter], Json.writes[Counter])
}

case class Counter(name: String, counter: Long)
