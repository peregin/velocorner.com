package velocorner.util

import com.typesafe.scalalogging.LazyLogging

import scala.io.Source
import scala.util.{Failure, Try}

/**
 * Utility to convert the weather code mappings into the model.
 *
 * # Group 5xx: Rain
 * # ID	Meaning	                    Icon BootstrapIcon
 * 500	light rain	                10d icon-weather-008
 * 501	moderate rain	            10d icon-weather-007
 */
case class WeatherCode(code: Int, meaning: String, bootstrapIcon: String)

object WeatherCodeUtils extends LazyLogging with CloseableResource {

  private lazy val code2Model = fromResources()

  def fromResources(): Map[Int, WeatherCode] = {
    val entries = withCloseable(Source.fromURL(getClass.getResource("/weather_codes.txt"))) {
      _.getLines()
        .map(_.trim)
        .filter(_.nonEmpty)
        .filter(!_.startsWith("#"))
        .map(line => Try(parse(line)))
        .toSeq
    }

    // log errors
    entries.foreach {
      case Failure(e) => logger.error("failed to parse line", e)
      case _ =>
    }

    entries
      .flatMap(_.toOption)
      .map(e => (e.code, e))
      .toMap
  }

  def bootstrapIcon(code: Long): String =
    code2Model.get(code.toInt).map(_.bootstrapIcon).getOrElse(sys.error(s"invalid weather code $code"))

  def parse(line: String): WeatherCode = {
    val sepFun = (c: Char) => c == ' ' || c == '\t'
    val ixCode = line.indexWhere(sepFun)
    if (ixCode < 0) throw new IllegalArgumentException(s"line has no code separator in $line")
    val code = line.substring(0, ixCode).toInt
    val remainder1 = line.substring(ixCode).trim
    val ixBootstrapIcon = remainder1.lastIndexWhere(sepFun)
    val bootstrapIcon = remainder1.substring(ixBootstrapIcon).trim
    val remainder2 = remainder1.substring(0, ixBootstrapIcon).trim
    val ixMeaning = remainder2.lastIndexWhere(sepFun)
    if (ixMeaning < 0) throw new IllegalArgumentException(s"line has no separator in $remainder2")
    val meaning = remainder2.substring(0, ixMeaning).trim
    WeatherCode(code = code, meaning = meaning, bootstrapIcon = bootstrapIcon)
  }
}
