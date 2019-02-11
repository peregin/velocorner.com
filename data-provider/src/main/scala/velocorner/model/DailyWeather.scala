package velocorner.model

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json.{Format, Json}
import velocorner.model.weather.{Weather, WeatherForecast}
import velocorner.util.WeatherCodeUtils

object DailyWeather {

  implicit val dateFormat = DateTimePattern.createShortFormatter
  implicit val entryFormat = Format[DailyWeather](Json.reads[DailyWeather], Json.writes[DailyWeather])

  def list(entries: Iterable[WeatherForecast]): Iterable[DailyWeather] = {
    entries
      .groupBy(wf => new DateTime(wf.timestamp).toLocalDate)
      .map{ case (day, wfList) => from(day, wfList.map(_.forecast)) }
      .toSeq.sortBy(_.day.toString)
  }

  def from(day: LocalDate, pointsForThisDay: Iterable[Weather]): DailyWeather = {
    val tempMin = pointsForThisDay.map(_.main.temp_min).min // celsius
    val tempMax = pointsForThisDay.map(_.main.temp_max).max // celsius
    val windMax = pointsForThisDay.map(_.wind.speed).max.toFloat * 3.6f // m/s to km/h
    val icon = WeatherCodeUtils.icon(WeatherCodeUtils.dailyWeatherCode(pointsForThisDay)) // bootstrap weather icon
    new DailyWeather(day, pointsForThisDay, tempMin, tempMax, windMax, icon)
  }
}

case class DailyWeather(day: LocalDate, points: Iterable[Weather],
                        temp_min: Float, temp_max: Float,
                        wind_max: Float,
                        icon: String)
