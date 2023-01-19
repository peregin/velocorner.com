package model

import org.joda.time.format.DateTimeFormat
import velocorner.api.{chart, Progress}
import velocorner.api.chart.{DailyPoint, DailySeries}
import velocorner.api.weather.WeatherForecast
import velocorner.model.{Units, YearlyProgress}

import scala.xml.Elem

object highcharts {

  def toDistanceSeries(items: Iterable[YearlyProgress], unit: Units.Entry): List[DailySeries] =
    toSeries(items, _.to(unit).distance)

  def toElevationSeries(items: Iterable[YearlyProgress], unit: Units.Entry): List[DailySeries] =
    toSeries(items, _.to(unit).elevation)

  private def toSeries(items: Iterable[YearlyProgress], fun: Progress => Double): List[DailySeries] =
    items
      .map(yp => chart.DailySeries(yp.year.toString, yp.progress.map(p => DailyPoint(p.day, fun(p.progress))).toList))
      .toList // must be a list because of the swagger spec generator

  implicit class PimpWeatherForecast(self: WeatherForecast) {
    val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
    val weather = self.forecast

    def to1DecimalPlace(value: Double): String = f"$value%.1f"

    def toXML: Elem = {
      val from = dateFormat.print(weather.dt)
      val to = dateFormat.print(weather.dt.plusHours(3))
      val precipitation = List(weather.rain.flatMap(_.`3h`), weather.snow.flatMap(_.`3h`), Some(0d)).flatten.sum
      <time from={from} to={to}>
        <symbol name={weather.weather.head.description} var={weather.weather.head.icon}/>
        <precipitation value={to1DecimalPlace(precipitation)}/>
        <windDirection deg={weather.wind.deg.toInt.toString}/>
        <windSpeed mps={to1DecimalPlace(weather.wind.speed * 3.6)}/>
        <temperature unit="celsius" value={to1DecimalPlace(weather.main.temp)}/>
        <pressure unit="hPa" value={to1DecimalPlace(weather.main.pressure)}/>
      </time>
    }
  }

  def toMeteoGramXml(items: List[WeatherForecast]): Elem = {
    val location = items.headOption.map(_.location).getOrElse("n/a")
    val (city, country) = location.lastIndexOf(',') match {
      case -1 => (location, "n/a")
      case ix => (location.substring(0, ix).trim, location.substring(ix + 1).trim)
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
