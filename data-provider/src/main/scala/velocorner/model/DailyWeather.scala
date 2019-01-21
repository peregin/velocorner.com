package velocorner.model

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json.{Format, Json}
import velocorner.model.weather.{Weather, WeatherForecast}

object DailyWeather {

  implicit val dateFormat = DateTimePattern.createShortFormatter
  implicit val entryFormat = Format[DailyWeather](Json.reads[DailyWeather], Json.writes[DailyWeather])

  def list(entries: Iterable[WeatherForecast]): Iterable[DailyWeather] = {
    entries
      .groupBy(wf => new DateTime(wf.timestamp).toLocalDate)
      .map{ case (day, wfList) => DailyWeather(day, wfList.map(_.forecast)) }
      .toSeq.sortBy(_.day.toString)
  }
}

case class DailyWeather(day: LocalDate, points: Iterable[Weather])
