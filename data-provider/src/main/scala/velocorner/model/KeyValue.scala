package velocorner.model

import play.api.libs.json.{Format, Json}

object KeyValue {
  implicit val entryFormat = Format[KeyValue](Json.reads[KeyValue], Json.writes[KeyValue])
}

case class KeyValue(key: String, `type`: String, value: String)
