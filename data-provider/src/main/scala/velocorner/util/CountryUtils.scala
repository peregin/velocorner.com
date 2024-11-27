package velocorner.util

import cats.implicits.{catsSyntaxOptionId, none}
import velocorner.model.CountryIso

/**
 * Utility:
 * 1./ to convert a country name (if given) into 2 letter ISO standard.
 * <city[,country]>
 * E.g.
 * Zurich,Switzerland = Zurich,CH
 * London = London
 * 2./ determine currency code for a country
 */
object CountryUtils {

  // Switzerland -> CH
  lazy val country2Code: Map[String, String] = readCountries() // lowercase name -> ISO code2
  // CH -> CHF
  lazy val code2Currency: Map[String, String] = readCurrencies()

  def readCountries(): Map[String, String] = {
    val countries = JsonIo.readReadFromResource[List[CountryIso]]("/countries.json")
    countries.map(ci => (ci.name.toLowerCase, ci.code)).toMap
  }

  def readCurrencies(): Map[String, String] =
    JsonIo.readReadFromResource[Map[String, String]]("/currencies.json")

  // converts location as Zurich,CH
  def iso(location: String): String = {
    val ix = location.indexWhere(_ == ',')
    if (ix > -1) {
      val country = location.substring(ix + 1).trim.toLowerCase
      country2Code.get(country).map(iso => s"${location.substring(0, ix).trim},$iso").getOrElse(location.trim)
    } else location.trim
  }

  // used to read the locations table, where everything is lowercase
  // converts to a format that is presented in the weather location widget:
  // adliswil, ch -> Adliswil, CH
  // abu dhabi, ae -> Abu Dhabi, AE
  // buenos aires, ar -> Buenos Aires, AR
  // budapest -> Budapest
  def beautify(location: String): String = {
    val (city, maybeCode) = location.lastIndexOf(',') match {
      case -1 => (location, none)
      case ix => (location.substring(0, ix).trim, location.substring(ix + 1).trim.some)
    }
    city.split(" ").map(_.trim.capitalize).mkString(" ") + (maybeCode match {
      case None                           => ""
      case Some(code) if code.length == 2 => s", ${code.toUpperCase}"
      case Some(name)                     => s", ${name.capitalize}"
    })
  }

  // some suggestions are similar, see:
  // "adliswil, ch"
  // "Adliswil, ch"
  // "adliswil"
  // "adliswil,CH"
  // "Adliswil,CH"
  // "Adliswil"
  // ----------------
  // normalized it to Adliswil,CH
  def normalize(locations: Iterable[String]): Iterable[String] = {
    val place2Locations = locations
      .map { l =>
        l.lastIndexOf(',') match {
          case -1 => (l, l)
          case ix => (l.substring(0, ix).trim, l)
        }
      }
      .map { case (place, iso) => (place.trim.toLowerCase, iso) }
      .groupBy(_._1)
      .view
      .mapValues(_.map(_._2))
    // get the best matching location from the list
    val bestLocations = place2Locations.map { case (_, list) =>
      list.fold("") { (res, item) =>
        (res, item) match {
          case ("", "")                                      => ""
          case ("", b)                                       => b
          case (a, "")                                       => a
          case (a, b) if a.head.isUpper && b.head.isLower    => a
          case (a, b) if a(0).isLower && b(0).isUpper        => b
          case (a, b) if a.contains(',') && !b.contains(',') => a
          case (a, b) if !a.contains(',') && b.contains(',') => b
          case (a, b) if a.last.isUpper && b.last.isLower    => a
          case (a, b) if a.last.isLower && b.last.isUpper    => b
          case (a, _)                                        => a
        }
      }
    }
    bestLocations.toList
  }
}
