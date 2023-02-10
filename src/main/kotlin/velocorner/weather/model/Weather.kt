package velocorner.weather.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * The response structures from openweathermap.
 * https://openweathermap.org/current
 */

/**
 * Response example.
 * <code><pre>
 * {
 *   "coord": {
 *     "lon": 8.52,
 *     "lat": 47.31
 *   },
 *   "weather": [
 *   {
 *     "id": 801,
 *     "main": "Clouds",
 *     "description": "few clouds",
 *     "icon": "02d"
 *   }
 *   ],
 *   "base": "stations",
 *   "main": {
 *     "temp": 7.57,
 *     "feels_like": 4.53,
 *     "temp_min": 5,
 *     "temp_max": 10,
 *     "pressure": 1012,
 *     "humidity": 81
 *   },
 *   "visibility": 10000,
 *   "wind": {
 *     "speed": 2.6,
 *     "deg": 20
 *   },
 *   "clouds": {
 *     "all": 20
 *   },
 *   "dt": 1602670919,
 *   "sys": {
 *     "type": 1,
 *     "id": 6932,
 *     "country": "CH",
 *     "sunrise": 1602654185,
 *     "sunset": 1602693616
 *   },
 *   "timezone": 7200,
 *   "id": 2661861,
 *   "name": "Adliswil",
 *   "cod": 200
 * }
 * </pre></code>
 */

@Serializable
data class WeatherDescription(
    val id: Int, // 800 - weather condition code
    val main: String, // "Clear"
    val description: String, // "clear sky"
    val icon: String // weather icon 04d
)

@Serializable
data class WeatherInfo(
    val temp: Float, // C
    val temp_min: Float, // C
    val temp_max: Float, // C
    val humidity: Float, // %
    val pressure: Float // hPa
)

@Serializer(forClass = OffsetDateTime::class)
object OffsetDateTimeSerializer : KSerializer<OffsetDateTime> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("WithCustomDefault", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        encoder.encodeLong(value.toInstant().toEpochMilli() / 1000)
    }

    override fun deserialize(decoder: Decoder): OffsetDateTime {
        val epoch = decoder.decodeLong()
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC)
    }
}

@Serializable
data class SunriseSunsetInfo(
    @Serializable(with = OffsetDateTimeSerializer::class)
    val sunrise: OffsetDateTime,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val sunset: OffsetDateTime
)

@Serializable
data class Coord(val lon: Double, val lat: Double)

@Serializable
data class SnowDescription(
    val `3h`: Double? = null // volume
)

@Serializable
data class RainDescription(
    val `3h`: Double? = null // mm
)

@Serializable
data class CloudDescription(
    val all: Int // %
)

@Serializable
data class WindDescription(
    val speed: Double, // m/s
    val deg: Double // degrees
)

@Serializable
data class City(
    val id: Long,
    val name: String, // plain name, such as Zurich, Budapest
    val country: String // ISO code 2 letter
)

// one of the entry points, the response contains a list of Weather structures, also stored in database
@Serializable
data class Weather(
    @Serializable(with = OffsetDateTimeSerializer::class)
    val dt: OffsetDateTime,
    val main: WeatherInfo,
    val weather: List<WeatherDescription>,
    val snow: SnowDescription? = null, // default value needed to signal optionality
    val rain: RainDescription? = null,
    val clouds: CloudDescription,
    val wind: WindDescription
)

// response expected from OpenWeatherMap API
@Serializable
data class CurrentWeatherResponse(
    val cod: Int,
    val weather: List<WeatherDescription>? = null,
    val main: WeatherInfo? = null,
    val sys: SunriseSunsetInfo? = null,
    val coord: Coord? = null,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val dt: OffsetDateTime? = null
)

// response of the service API and persisted in the caching layer (converted from CurrentWeatherResponse)
@Serializable
data class CurrentWeather(
    val location: String, // city[, country iso 2 letters]
    @Serializable(with = OffsetDateTimeSerializer::class)
    val timestamp: OffsetDateTime,
    val bootstrapIcon: String, // bootstrap icon derived from the current weather code
    val current: WeatherDescription,
    val info: WeatherInfo,
    val sunriseSunset: SunriseSunsetInfo,
    val coord: Coord
)

// response expected from OpenWeatherMap API
@Serializable
data class ForecastWeatherResponse(
    val cod: String,
    val list: List<Weather>? = null,
    val city: City? = null
)

// response of the service API and persisted in the caching layer (converted from ForecastWeatherResponse)
// https://openweathermap.org/forecast5#format
@Serializable
data class ForecastWeather(
    val location: String, // city[, country iso 2 letters]
    @Serializable(with = OffsetDateTimeSerializer::class)
    val timestamp: OffsetDateTime,
    val forecast: Weather
)
