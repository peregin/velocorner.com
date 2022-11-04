package velocorner.api.weather

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json.{Format, Json}
import velocorner.model.DateTimePattern
import velocorner.util.WeatherCodeUtils

// needed for meteogram
object DailyWeather {

  implicit val dateFormat = DateTimePattern.createShortFormatter
  implicit val entryFormat = Format[DailyWeather](Json.reads[DailyWeather], Json.writes[DailyWeather])

  def list(entries: List[WeatherForecast]): List[DailyWeather] =
    entries
      .groupBy(wf => new DateTime(wf.timestamp).toLocalDate)
      .map { case (day, wfList) => from(day, wfList.map(_.forecast)) }
      .toList
      .sortBy(_.day.toString)

  import Ordering.Float.IeeeOrdering

  def from(day: LocalDate, pointsForThisDay: List[Weather]): DailyWeather = {
    val tempMin = pointsForThisDay.map(_.main.temp_min).min // celsius
    val tempMax = pointsForThisDay.map(_.main.temp_max).max // celsius
    val windMax = pointsForThisDay.map(_.wind.speed).max(Ordering.Double.IeeeOrdering).toFloat * 3.6f // m/s to km/h
    val bootstrapIcon = WeatherCodeUtils.bootstrapIcon(WeatherCodeUtils.dailyWeatherCode(pointsForThisDay)) // bootstrap weather icon
    new DailyWeather(day, pointsForThisDay, tempMin, tempMax, windMax, bootstrapIcon)
  }
}

case class DailyWeather(day: LocalDate, points: List[Weather], temp_min: Float, temp_max: Float, wind_max: Float, bootstrapIcon: String)
