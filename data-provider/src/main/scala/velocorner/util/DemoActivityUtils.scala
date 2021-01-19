package velocorner.util

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import velocorner.api.strava.Activity
import velocorner.model.strava.{Athlete, Gear}

import cats.implicits._

import scala.util.Random

// utility to generate demo activities, to show case for a new user what the site does.
object DemoActivityUtils {

  val rnd = new Random()

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
    val timeIt = LazyList.iterate(from)(_.plusDays(rnd.between(1, 5))).takeWhile(_.compareTo(until) < 0).map { day =>
      val movingTime = rnd.between(60000, 6000000)
      Activity(
        id = day.toDate.getTime,
        resource_state = 0,
        external_id = None,
        upload_id = None,
        athlete = demoAthlete,
        name = rnd.alphanumeric.take(10).toString,
        distance = rnd.between(3000f, 120000f), // meters
        moving_time = movingTime,
        elapsed_time = movingTime,
        total_elevation_gain = rnd.between(50f, 1800f),
        `type` = "Ride",
        start_date = day.toDateTimeAtCurrentTime,
        start_date_local = None,
        average_speed: Option[Float],
        max_speed: Option[Float],
        average_cadence: Option[Float],
        average_temp: Option[Float],
        average_watts: Option[Float],
        max_watts: Option[Float],
        average_heartrate: Option[Float],
        max_heartrate: Option[Float],
        gear_id: Option[String],
        start_latitude: Option[Float],
        start_longitude: Option[Float],
        commute: Option[Boolean],
        elev_high: Option[Float],
        elev_low: Option[Float],
        pr_count: Option[Int]
      )
      day
    }
    println(timeIt.toList)
    Nil
  }
}
