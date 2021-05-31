package velocorner.model

object ActionType {

  sealed trait Entry

  case object Distance extends Entry
  case object Elevation extends Entry

  def from(s: String): Entry = s.trim.toLowerCase match {
    case "distance"  => Distance
    case "elevation" => Elevation
    case unknown     => throw new IllegalArgumentException(s"unknown action type: $unknown")
  }

  def apply(s: String): Entry = from(s)
}
