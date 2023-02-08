package velocorner.weather.service

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import velocorner.weather.model.CurrentWeatherResponse
import velocorner.weather.model.ForecastWeatherResponse

class OpenWeatherFeed(val apiKey: String, val client: HttpClient) {

    private val baseUrl = "https://api.openweathermap.org/data/2.5"
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun current(location: String): CurrentWeatherResponse? =
        get<CurrentWeatherResponse>("weather", location)

    suspend fun forecast(location: String): ForecastWeatherResponse? =
        get<ForecastWeatherResponse>("forecast", location)

    internal suspend inline fun <reified T> get(path: String, location: String): T? {
        val response = client.get("$baseUrl/$path") {
            parameter("q", location)
            parameter("appid", apiKey)
            parameter("units", "metric")
            parameter("lang", "en")
        }
        return runCatching { json.decodeFromString<T>(response.bodyAsText()) }.getOrNull()
    }
}