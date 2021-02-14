package velocorner.model.withings

import org.joda.time.DateTime
import play.api.libs.json._
import velocorner.model.EpochFormatter

/** Represents a GetMeasures from the Withings feed:
  * https://developer.health.nokia.com/api/doc#api-Measure-get_measure
  *
  * The model Measurement is persisted in the storage and it is is extracted from this response.
  *
  * {
  *  "status": 0,                      // Response status; 0 : Operation was successful
  *  "body": {
  *    "updatetime": 1515353923,
  *    "measuregrps": [
  *      {
  *        "grpid": 999543286,         // Id of the measure group
  *        "attrib": 0,                // The way the measure was attributed to the user (captured by a device, ambiguous, manual, etc.)
  *        "date": 1515190616,         // Date as unix epoch
  *        "category": 1,              // Category for the measures in the group (1 for real measurements, 2 for user objectives)
  *        "measures": [
  *          {
  *            "value": 76567,         // Value for the measure in S.I units (kilogram, meters, etc.)
  *            "type": 1,              // Type of the measure.
  *            "unit": -3              // Power of ten the "value" parameter should be multiplied to to get the real value.
  *          }
  *        ]
  *      }
  *    ],
  *    "timezone": "Europe\/Zurich"
  *  }
  * }
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

object MeasureResponse {

  implicit val format = Format[MeasureResponse](Json.reads[MeasureResponse], Json.writes[MeasureResponse])
}

case class MeasureResponse(
    status: Int,
    body: MeasuresBody
) {

  def convert(athleteId: Int): List[Measurement] = {
    require(status == 0, s"status is not ok, it is $status")
    body.measuregrps.flatMap { mg =>
      mg.measures.map { m =>
        Measurement(
          id = s"${mg.grpid}_${m.`type`}",
          `type` = MeasurementType.value2Type.getOrElse(m.`type`, sys.error(s"unknown measurement type: ${m.`type`}")),
          value = m.value * math.pow(10, m.unit),
          athleteId = athleteId,
          updateTime = mg.date
        )
      }
    }
  }
}
