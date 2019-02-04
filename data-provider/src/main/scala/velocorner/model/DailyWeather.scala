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
    val min = pointsForThisDay.map(_.main.temp_min).min
    val max = pointsForThisDay.map(_.main.temp_max).max
    val codes = pointsForThisDay.flatMap(_.weather).map(_.id)
    val minCode = if (codes.isEmpty) WeatherCodeUtils.clearSkyCode else codes.min
    val icon = WeatherCodeUtils.icon(minCode)
    new DailyWeather(day, pointsForThisDay, min, max, icon)
  }
}

case class DailyWeather(day: LocalDate, points: Iterable[Weather],
                        temp_min: Float, temp_max: Float,
                        icon: String)
