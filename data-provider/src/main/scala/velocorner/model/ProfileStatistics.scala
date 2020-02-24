package velocorner.model

import org.joda.time.LocalDate
import play.api.libs.json.{Format, Json}
import scalaz.syntax.std.boolean._
import velocorner.api.Progress

case class ProfileStatistics(yearlyPercentile: Int, estimate: Progress, progress: Progress, commute: Progress)

object ProfileStatistics {

  val zero = ProfileStatistics(0, Progress.zero, Progress.zero, Progress.zero)

  implicit val totalFormat = Format[ProfileStatistics](Json.reads[ProfileStatistics], Json.writes[ProfileStatistics])

  def from(now: LocalDate, ytdProgress: Progress, ytdCommute: Progress): ProfileStatistics = {
    val dayToDate = now.dayOfYear().get()
    val daysInYear = now.year().isLeap ? 366 | 365
    from(dayToDate, daysInYear, ytdProgress, ytdCommute)
  }

  def from(dayToDate: Int, daysInYear: Int, ytdProgress: Progress, ytdCommute: Progress): ProfileStatistics = {
    // note that this should not be a linear function between march and october - 20->80% - there are more ride activities
    val f = daysInYear.toDouble / dayToDate.max(1)
    val percentile = dayToDate.toDouble * 100 / daysInYear
    ProfileStatistics(yearlyPercentile = percentile.toInt.min(100), ytdProgress * f, ytdProgress, ytdCommute)
  }
}
