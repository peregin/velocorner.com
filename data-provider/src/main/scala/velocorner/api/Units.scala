package velocorner.api

object Units {

  sealed abstract class Entry(
      val speedLabel: String,
      val distanceLabel: String,
      val elevationLabel: String,
      val temperatureLabel: String
  )

  case object Metric extends Entry("km/h", "km", "m", "°C")
  case object Imperial extends Entry("mph", "mi", "ft", "°F")
}
