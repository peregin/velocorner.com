package velocorner.util

import org.slf4s.Logging
import velocorner.model.weather.WeatherCode

import scala.io.Source
import scala.util.{Failure, Try}

/**
  * Utility to convert the weather code mappings into the model.
  *
  * # Group 5xx: Rain
  * # ID	Meaning	                    Icon BootstrapIcon
  * 500	light rain	                  10d icon-weather-008
  * 501	moderate rain	                10d icon-weather-007
  *
  */
object WeatherCodeUtils extends Logging {

  lazy val code2Model = fromResources()

  val clearSkyCode = 800

  def fromResources(): Map[Int, WeatherCode] = {
    val entries = Source
      .fromURL(getClass.getResource("/weather_codes.txt"))
      .getLines()
      .map(_.trim)
      .filter(_.nonEmpty)
      .filter(!_.startsWith("#"))
      .map(line => Try(parse(line)))
      .toSeq

    // log errors
    val failures = entries
      .filter(_.isFailure)
      .map{ case Failure(e) => e }
    if (failures.size > 0) failures.foreach(e => log.error("failed to parse line", e))

    entries
      .flatMap(_.toOption)
      .map(e => (e.code, e))
      .toMap
  }

  def icon(code: Long): String = code2Model.get(code.toInt).map(_.icon).getOrElse(sys.error(s"invalid weather code $code"))

  def parse(line: String): WeatherCode = {
    val sepFun = (c: Char) => c == ' ' || c == '\t'
    val ixCode = line.indexWhere(sepFun)
    if (ixCode < 0) throw new IllegalArgumentException(s"line has no code separator in $line")
    val code = line.substring(0, ixCode).toInt
    val remainder1 = line.substring(ixCode).trim
    val ixBootstrapIcon = remainder1.lastIndexWhere(sepFun)
    if (ixCode < 0) throw new IllegalArgumentException(s"line has no icon separator in $remainder1")
    val bootstrapIcon = remainder1.substring(ixBootstrapIcon).trim
    val remainder2 = remainder1.substring(0, ixBootstrapIcon).trim
    val ixMeaning = remainder2.lastIndexWhere(sepFun)
    if (ixMeaning < 0) throw new IllegalArgumentException(s"line has no separator in $remainder2")
    val meaning = remainder2.substring(0, ixMeaning).trim
    WeatherCode(code = code, meaning = meaning, icon = bootstrapIcon)
  }
}
