package velocorner.model

import org.joda.time.DateTime
import play.api.libs.json._

/**
  * Represents a GetMeasures from the Withings feed:
  * https://developer.health.nokia.com/api/doc#api-Measure-get_measure
  *

{
  "status": 0,
  "body": {
    "updatetime": 1515353923,
    "measuregrps": [
      {
        "grpid": 999543286,
        "attrib": 0,
        "date": 1515190616,
        "category": 1,
        "measures": [
          {
            "value": 76567,
            "type": 1,
            "unit": -3
          }
        ]
      }
    ],
    "timezone": "Europe\/Zurich"
  }
}

  */

object EpochFormatter {

  // epoch to DateTime nad vice versa
  def create = Format[DateTime](new Reads[DateTime] {
    override def reads(json: JsValue): JsResult[DateTime] = {
      val epoch = json.asInstanceOf[JsNumber].value.toLong * 1000
      JsSuccess(new DateTime(epoch))
    }
  }, JodaWrites.JodaDateTimeNumberWrites)
}

object MeasuresGroup {

  implicit val dateTimeFormat = EpochFormatter.create
  implicit val format = Format[MeasuresGroup](Json.reads[MeasuresGroup], Json.writes[MeasuresGroup])
}

case class MeasuresGroup(
  grpid: Long,
  attrib: Int,
  date: DateTime,
  category: Int
)

object MeasuresBody {

  implicit val dateTimeFormat = EpochFormatter.create
  implicit val format = Format[MeasuresBody](Json.reads[MeasuresBody], Json.writes[MeasuresBody])
}

case class MeasuresBody(
  updatetime: DateTime,
  timezone: String,
  measuregrps: List[MeasuresGroup]
)

object MeasuresResponse {

  implicit val format = Format[MeasuresResponse](Json.reads[MeasuresResponse], Json.writes[MeasuresResponse])
}

case class MeasuresResponse(
  status: Int,
  body: MeasuresBody
)
