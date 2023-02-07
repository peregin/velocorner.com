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

    override fun serialize(encoder: Encoder, obj: OffsetDateTime) {
        encoder.encodeLong(obj.toInstant().toEpochMilli() / 1000)
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

// response expected from OpenWeather API
@Serializable
data class CurrentWeatherResponse(
    val cod: Int,
    val weather: List<WeatherDescription>?,
    val main: WeatherInfo?,
    val sys: SunriseSunsetInfo?,
    val coord: Coord?,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val dt: OffsetDateTime?
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
