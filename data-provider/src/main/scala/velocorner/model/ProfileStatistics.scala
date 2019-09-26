package velocorner.model

import org.joda.time.LocalDate
import play.api.libs.json.{Format, Json}
import scalaz.syntax.std.boolean._

case class ProfileStatistics(estimate: Progress, progress: Progress)

object ProfileStatistics {

  implicit val totalFormat = Format[ProfileStatistics](Json.reads[ProfileStatistics], Json.writes[ProfileStatistics])

  def from(now: LocalDate, ytdProgress: Progress): ProfileStatistics = {
    val dayToDate = now.dayOfYear().get()
    val daysInYear = now.year().isLeap() ? 366 | 365
    from(dayToDate, daysInYear, ytdProgress)
  }

  def from(dayToDate: Int, daysInYear: Int, ytdProgress: Progress): ProfileStatistics = {
    val f = daysInYear.toDouble / dayToDate.max(1)
    ProfileStatistics(ytdProgress * f, ytdProgress)
  }
}
