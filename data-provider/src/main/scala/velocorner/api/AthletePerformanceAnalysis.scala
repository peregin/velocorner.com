package velocorner.api

import org.joda.time.DateTime

case class AthletePerformanceAnalysis(
    athleteId: Long,
    fingerprint: String,
    basedOn: String,
    summary: String,
    createdAt: DateTime
)
