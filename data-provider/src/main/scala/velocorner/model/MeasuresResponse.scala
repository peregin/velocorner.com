package velocorner.model

import org.joda.time.DateTime
import play.api.libs.json._

/**
  * Represents a GetMeasures from the Withings feed:
  * https://developer.health.nokia.com/api/doc#api-Measure-get_measure
  *
  * The model persisted in the storage is extracted from this response.
  *

{
  "status": 0,                      // Response status; 0 : Operation was successful
  "body": {
    "updatetime": 1515353923,
    "measuregrps": [
      {
        "grpid": 999543286,         // Id of the measure group
        "attrib": 0,                // The way the measure was attributed to the user (captured by a device, ambiguous, manual, etc.)
        "date": 1515190616,         // Date as unix epoch
        "category": 1,              // Category for the measures in the group (1 for real measurements, 2 for user objectives)
        "measures": [
          {
            "value": 76567,         // Value for the measure in S.I units (kilogram, meters, etc.)
            "type": 1,              // Type of the measure.
            "unit": -3              // Power of ten the "value" parameter should be multiplied to to get the real value.
          }
        ]
      }
    ],
    "timezone": "Europe\/Zurich"
  }
}
  *
  *  Measurement type is one of the following:
  *
  *  1 : Weight (kg)
  *  4 : Height (meter)
  *  5 : Fat Free Mass (kg)
  *  6 : Fat Ratio (%)
  *  8 : Fat Mass Weight (kg)
  *  9 : Diastolic Blood Pressure (mmHg)
  *  10 : Systolic Blood Pressure (mmHg)
  *  11 : Heart Pulse (bpm)
  *  12 : Temperature
  *  54 : SP02(%)
  *  71 : Body Temperature
  *  73 : Skin Temperature
  *  76 : Muscle Mass
  *  77 : Hydration
  *  88 : Bone Mass
  *  91 : Pulse Wave Velocity
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


object MeasuresEntry {

  implicit val format = Format[MeasuresEntry](Json.reads[MeasuresEntry], Json.writes[MeasuresEntry])
}

case class MeasuresEntry(
  value: Int,
  `type`: Int,
  unit: Int
)

object MeasuresGroup {

  implicit val dateTimeFormat = EpochFormatter.create
  implicit val format = Format[MeasuresGroup](Json.reads[MeasuresGroup], Json.writes[MeasuresGroup])
}

case class MeasuresGroup(
  grpid: Long,
  attrib: Int,
  date: DateTime,
  category: Int,
  measures: List[MeasuresEntry]
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
