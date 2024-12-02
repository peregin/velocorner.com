package velocorner.backend.util

import velocorner.backend.model.Activity
import velocorner.backend.model.Athlete
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.random.Random

object DemoActivityUtil {
    private val rnd = Random.Default

    internal val titles = listOf(
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

    private val demoAthlete = Athlete(
        id = 1L,
        resourceState = 0,
        firstname = "Rider",
        lastname = "Demo",
        profileMedium = null,
        city = "Zurich",
        country = "Switzerland",
        bikes = null,
        shoes = null
    )

    fun generate(until: LocalDate = LocalDate.now(), yearsBack: Int = 4): Sequence<Activity> {
        val from = until.minusYears(yearsBack.toLong())
            .withDayOfMonth(1)
            .withMonth(1)

        return generateSequence(from) { date ->
            date.plusDays(rnd.nextLong(1, 5))
        }
            .takeWhile { it.isBefore(until) }
            .map { day ->
                // add less noise to each year, then the stat will look like continuous yearly improvement
                val yearlyNoise = rnd.nextInt(yearsBack + 1 - until.year + day.year)
                val movingTime = rnd.nextInt(60000, 6000000)
                val distanceInMeter = rnd.nextDouble() * (120000 - 3000) + 3000
                val elevationInMeter = rnd.nextDouble() * (1100 - 50) + 50 + (yearlyNoise * 500)

                Activity(
                    id = day.toEpochDay(),
                    resource_state = 0,
                    external_id = null,
                    uploadId = null,
                    athlete = demoAthlete,
                    name = generateRandomString(10),
                    distance = distanceInMeter,
                    moving_time = movingTime,
                    elapsed_time = movingTime,
                    total_elevation_gain = elevationInMeter,
                    type = "Ride",
                    start_date = OffsetDateTime.now().withYear(day.year)
                        .withMonth(day.monthValue)
                        .withDayOfMonth(day.dayOfMonth),
                    start_date_local = null,
                    average_speed = null,
                    max_speed = null,
                    average_cadence = null,
                    average_temp = null
                )
            }
    }

    fun generateTitles(max: Int): List<String> {
        return generateSequence { titles[rnd.nextInt(titles.size)] }
            .take(max)
            .toList()
    }

    private fun generateRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random(rnd) }
            .joinToString("")
    }
}
