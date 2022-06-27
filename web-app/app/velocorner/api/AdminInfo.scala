package velocorner.api

import play.api.libs.json.{Format, Json}

object AdminInfo {
  implicit val format = Format[AdminInfo](Json.reads[AdminInfo], Json.writes[AdminInfo])
}

case class AdminInfo(
    accounts: Long,
    activeAccounts: Long,
    activities: Long,
    brands: Long
)
