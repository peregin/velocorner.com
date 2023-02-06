package velocorner.weather.model

import kotlinx.serialization.Serializable

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
data class WeatherCurrentResponse(val cod: Int)
