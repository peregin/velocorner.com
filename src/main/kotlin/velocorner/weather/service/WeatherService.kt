package velocorner.weather.service

import org.slf4j.LoggerFactory
import velocorner.weather.model.CurrentWeather
import velocorner.weather.model.CurrentWeatherResponse
import velocorner.weather.util.WeatherCodeUtil
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

// it uses data from the cache/storage if was queried within the `refreshTimeout`
class WeatherService(val feed: OpenWeatherFeed, refreshTimeout: Duration = 15.minutes) {

    val logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun current(location: String): CurrentWeather? {
        val reply = feed.current(location)
        return convert(location, reply)
    }

    fun convert(location: String, reply: CurrentWeatherResponse?): CurrentWeather? {
        return reply?.weather?.let { wd ->
            reply.sys?.let { sy ->
                reply.main?.let { ma ->
                    reply.coord?.let { co ->
                        CurrentWeather(
                            location = location,
                            timestamp = OffsetDateTime.now(ZoneId.of("UTC")),
                            bootstrapIcon = WeatherCodeUtil.bootstrapIcon(wd[0].id),
                            current = wd[0],
                            info = ma,
                            sunriseSunset = sy,
                            coord = co
                        )
                    }
                }
            }
        }
    }
}