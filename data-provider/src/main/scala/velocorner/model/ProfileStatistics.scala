package velocorner.model

import org.joda.time.LocalDate

import scalaz.syntax.std.boolean._

case class ProfileStatistics(estimate: Progress, progress: Progress)

object ProfileStatistics {

  def from(now: LocalDate, ytdProgress: Progress): ProfileStatistics = {
    val dtd = now.dayOfYear().get()
    val daysInYear = now.year().isLeap() ? 366 | 365
    ProfileStatistics(ytdProgress, ytdProgress)
  }
}
