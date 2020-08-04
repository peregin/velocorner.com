package velocorner.util

import velocorner.model.CountryIso

/**
  * Utility to convert a country name (if given) into 2 letter ISO standard.
  * <city[,country]>
  * E.g.
  * Zurich,Switzerland = Zurich,CH
  * London = London
  */
object CountryIsoUtils {

  lazy val country2Code: Map[String, String] = fromResources()

  def fromResources(): Map[String, String] = {
    val countries = JsonIo.readReadFromResource[List[CountryIso]]("/countries.json")
    countries.map(ci => (ci.name.toLowerCase, ci.code)).toMap
  }

  // converts location as Zurich,CH
  def iso(location: String): String = {
    val ix = location.indexWhere(_ == ',')
    val isoLocation = if (ix > -1) {
      val country = location.substring(ix + 1).trim.toLowerCase
      country2Code.get(country).map(iso => s"${location.substring(0, ix).trim},$iso").getOrElse(location.trim)
    } else location.trim
    isoLocation
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
    // "adliswil" -> List("adliswil, ch")
    val place2Locations = locations.map { l =>
      l.lastIndexOf(',') match {
        case -1 => (l, l)
        case ix => (l.substring(0, ix).trim, l)
      }
    }.map { case (place, iso) => (place.trim.toLowerCase, iso) }.groupBy(_._1).view.mapValues(_.map(_._2))
    // get the best matching location from the list
    val bestLocations = place2Locations.map { case (_, list) =>
      list.fold("") { (res, item) =>
        (res, item) match {
          case ("", "") => ""
          case ("", b) => b
          case (a, "") => a
          case (a, b) if a.head.isUpper && b.head.isLower => a
          case (a, b) if a(0).isLower && b(0).isUpper => b
          case (a, b) if a.contains(',') && !b.contains(',') => a
          case (a, b) if !a.contains(',') && b.contains(',') => b
          case (a, b) if a.last.isUpper && b.last.isLower => a
          case (a, b) if a.last.isLower && b.last.isUpper => b
          case (a, _) => a
        }
      }
    }
    bestLocations.toList
  }
}
