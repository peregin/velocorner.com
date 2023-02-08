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

    suspend fun current(location: String): CurrentWeatherResponse? {
        val response = get("weather", location)
        return json.decodeFromString<CurrentWeatherResponse>(response.bodyAsText())
    }

    suspend fun forecast(location: String): ForecastWeatherResponse? {
        val response = get("forecast", location)
        return json.decodeFromString<ForecastWeatherResponse>(response.bodyAsText())
    }

    internal suspend fun get(path: String, location: String): HttpResponse = client.get("$baseUrl/$path") {
        parameter("q", location)
        parameter("appid", apiKey)
        parameter("units", "metric")
        parameter("lang", "en")
    }
}