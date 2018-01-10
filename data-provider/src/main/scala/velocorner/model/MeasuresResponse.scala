package velocorner.model

import org.joda.time.DateTime
import play.api.libs.json._

/**
  * Represents a GetMeasures from the Withings feed:
  *
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
      },
      {
        "grpid": 200333696,
        "attrib": 0,
        "date": 1397305600,
        "category": 1,
        "measures": [
          {
            "value": 73163,
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

object MeasuresBody {

  // epoch to DateTime
  implicit val dateTimeFormat = Format[DateTime](new Reads[DateTime] {
    override def reads(json: JsValue): JsResult[DateTime] = {
      val epoch = json.asInstanceOf[JsNumber].value.toLong * 1000
      JsSuccess(new DateTime(epoch))
    }
  }, JodaWrites.JodaDateTimeNumberWrites)
  implicit val format = Format[MeasuresBody](Json.reads[MeasuresBody], Json.writes[MeasuresBody])
}

case class MeasuresBody(
  updatetime: DateTime,
  timezone: String
)

object MeasuresResponse {

  implicit val format = Format[MeasuresResponse](Json.reads[MeasuresResponse], Json.writes[MeasuresResponse])
}

case class MeasuresResponse(
  status: Int,
  body: MeasuresBody
)
