import org.joda.time.{LocalDate, LocalDateTime}
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._
import velocorner.model.weather.WeatherForecast
import velocorner.model.{AthleteDailyProgress, DateTimePattern, Progress, YearlyProgress}

import scala.xml.Elem

/**
  * Created by levi on 29/01/16.
  */
package object highcharts {

  object DailyPoint {
    implicit val dateFormat = DateTimePattern.createShortFormatter
    implicit val pointFormat = Format[DailyPoint](Json.reads[DailyPoint], Json.writes[DailyPoint])
  }

  case class DailyPoint(day: LocalDate, value: Double) {

    def getMonth = day.getMonthOfYear - 1 // in javascript date starts with 0
    def getDay = day.getDayOfMonth
  }


  object DailySeries {
    implicit val seriesFormat = Format[DailySeries](Json.reads[DailySeries], Json.writes[DailySeries])
  }

  case class DailySeries(name: String, series: Iterable[DailyPoint]) {

    def aggregate() = {
      val aggregatedSeries = series.toSeq.reverse.scanLeft(DailyPoint(LocalDate.now(), 0))((accu, i) =>
        DailyPoint(i.day, accu.value + i.value)).tail
      DailySeries(name, aggregatedSeries)
    }
  }


  def toDistanceSeries(items: Iterable[YearlyProgress]) = toSeries(items, _.distance)

  def toElevationSeries(items: Iterable[YearlyProgress]) = toSeries(items, _.elevation)

  private def toSeries(items: Iterable[YearlyProgress], fun: Progress => Double): Iterable[DailySeries] = {
    items.map(yp => DailySeries(yp.year.toString, yp.progress.map(p => DailyPoint(p.day, fun(p.progress)))))
  }


  def toAthleteDistanceSeries(items: Iterable[AthleteDailyProgress]) = toAthleteSeries(items, _.distance)

  def toAthleteElevationSeries(items: Iterable[AthleteDailyProgress]) = toAthleteSeries(items, _.elevation)

  private def toAthleteSeries(items: Iterable[AthleteDailyProgress], fun: Progress => Double): Iterable[DailySeries] = {
    items.groupBy(_.athleteId).map{case (athleteId, list) =>
      DailySeries(athleteId.toString, list.map(e => DailyPoint(e.dailyProgress.day, fun(e.dailyProgress.progress))))
    }
  }

  implicit class PimpWeatherForecast(self: WeatherForecast) {
    val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
    val weather = self.forecast

    def to1DecimalPlace(value: Double): String = f"$value%.1f"

    def toXML: Elem = {
      val from = dateFormat.print(weather.dt)
      val to = dateFormat.print(weather.dt.plusHours(3))
      val precipitation = weather.rain.flatMap(_.`3h`).orElse(weather.snow.flatMap(_.`3h`)).getOrElse(0d)
      <time from={from} to={to}>
        <symbol name={weather.weather.head.description} var={weather.weather.head.icon}/>
        <precipitation value={to1DecimalPlace(precipitation)}/>
        <windDirection deg={weather.wind.deg.toInt.toString}/>
        <windSpeed mps={to1DecimalPlace(weather.wind.speed)}/>
        <temperature unit="celsius" value={to1DecimalPlace(weather.main.temp)}/>
        <pressure unit="hPa" value={to1DecimalPlace(weather.main.pressure)}/>
      </time>
    }
  }

  def toMeteoGramXml(items: Iterable[WeatherForecast]): Elem = {
    val location = {items.headOption.map(_.location).getOrElse("n/a")}
    val (city, country) = location.lastIndexOf(',') match {
      case -1 => (location, "n/a")
      case ix => (location.substring(0, ix).trim, location.substring(ix+1).trim)
    }
<weatherdata>
  <location>
    <name>{city}</name>
    <country>{country}</country>
  </location>
  <credit>
    <link text="Weather forecast" url="http://velocorner.com"/>
  </credit>
  <forecast>
    <tabular>
      {items.map(_.toXML)}
    </tabular>
  </forecast>
</weatherdata>
  }
}
