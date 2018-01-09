package velocorner.model

import play.api.libs.json.{Format, Json}

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

  implicit val format = Format[MeasuresBody](Json.reads[MeasuresBody], Json.writes[MeasuresBody])
}

case class MeasuresBody(
  timezone: String
)

object MeasuresResponse {

  implicit val format = Format[MeasuresResponse](Json.reads[MeasuresResponse], Json.writes[MeasuresResponse])
}

case class MeasuresResponse(
  status: Int,
  body: MeasuresBody
)
