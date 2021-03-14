package velocorner.util

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import velocorner.api.strava.Activity
import velocorner.model.strava.{Athlete, Gear}

import cats.implicits._

import scala.util.Random

// utility to generate demo activities, to show case for a new user what the site does.
object DemoActivityUtils {

  val rnd = new Random()

  val titles = List(
    "Morning Ride",
    "Evening Ride",
    "ZZP Ride",
    "In the Alps",
    "Gotthard Pass",
    "Chamonix Round",
    "Passo dello Stelvio",
    "Utah MTB",
    "Alpe d'Huez",
    "Col du Tourmalet",
    "Passo Pordoi",
    "Mont Ventoux",
    "Finale Ligure Enduro"
  )

  val demoAthlete = Athlete(
    id = 1L,
    resource_state = 0,
    firstname = "Rider".some,
    lastname = "Demo".some,
    profile_medium = None, // URL to a 62x62 pixel profile picture
    city = "Zurich".some,
    country = "Switzerland".some,
    bikes = None,
    shoes = None
  )

  def generate(until: LocalDate = LocalDate.now(DateTimeZone.UTC), yearsBack: Int = 4): Iterable[Activity] = {
    val from = until.minusYears(yearsBack).withDayOfMonth(1).withMonthOfYear(1)
    val activityStream = LazyList.iterate(from)(_.plusDays(rnd.between(1, 5))).takeWhile(_.compareTo(until) < 0).map { day =>
      // add less noise to each year that, then the stat will look like continuous yearly improvement
      val yearlyNoise = rnd.nextInt(yearsBack + 1 - until.getYear + day.getYear)
      val movingTime = rnd.between(60000, 6000000)
      val distanceInMeter = rnd.between(3000f, 120000f)
      val elevationInMeter = rnd.between(50f, 1100f) + (yearlyNoise * 500)
      Activity(
        id = day.toDate.getTime,
        resource_state = 0,
        external_id = None,
        upload_id = None,
        athlete = demoAthlete,
        name = rnd.alphanumeric.take(10).toString,
        distance = distanceInMeter,
        moving_time = movingTime,
        elapsed_time = movingTime,
        total_elevation_gain = elevationInMeter,
        `type` = "Ride",
        start_date = day.toDateTimeAtCurrentTime,
        start_date_local = None,
        average_speed = None,
        max_speed = None,
        average_cadence = None,
        average_temp = None,
        average_watts = None,
        max_watts = None,
        average_heartrate = None,
        max_heartrate = None,
        gear_id = None,
        start_latitude = None,
        start_longitude = None,
        commute = None,
        elev_high = None,
        elev_low = None,
        pr_count = None
      )
    }
    activityStream
  }

  def generateTitles(max: Int): List[String] = {
    val n = titles.size
    LazyList.continually(titles(rnd.nextInt(n))).take(max).toList
  }
}
