package velocorner.weather.service

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class OpenWeatherFeed(val apiKey: String, val client: HttpClient) {

    private val baseUrl = "https://api.openweathermap.org/data/2.5"

    suspend fun current(location: String): String? {
        val response = client.get("$baseUrl/weather") {
            parameter("q", location)
            parameter("appid", apiKey)
            parameter("units", "metric")
            parameter("lang", "en")
        }
        return response.bodyAsText()
    }
}