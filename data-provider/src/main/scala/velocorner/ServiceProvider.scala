package velocorner

object ServiceProvider extends Enumeration {
  val Strava = Value("strava")
  val Withings = Value("withings")
  val Windy = Value("windy")
  val Crawler = Value("crawler")
}
