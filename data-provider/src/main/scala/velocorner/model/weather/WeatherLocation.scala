package velocorner.model.weather

import velocorner.model.CountryIso

/**
  * Utility to convert a country name (if given) into 2 letter ISO standard.
  * <city[,country]>
  * E.g.
  * Zurich,Switzerland = Zurich,CH
  * London = London
  */
object WeatherLocation {

  lazy val country2Code = CountryIso.fromResources()

  def iso(location: String): String = {
    val ix = location.indexWhere(_ == ',')
    val isoLocation = if (ix > -1) {
      val country = location.substring(ix+1).trim.toLowerCase
      country2Code.get(country).map(iso => s"${location.substring(0, ix).trim},$iso").getOrElse(location.trim)
    } else location.trim
    isoLocation
  }
}
