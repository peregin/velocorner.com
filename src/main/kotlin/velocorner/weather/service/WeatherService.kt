package velocorner.weather.service

import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

// it uses data from the cache/storage if was queried within the `refreshTimeout`
class WeatherService(val feed: OpenWeatherFeed, refreshTimeout: Duration = 15.minutes) {

    val logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun current(location: String): String? {
        val reply = feed.current(location)
        return reply.toString()
    }
}