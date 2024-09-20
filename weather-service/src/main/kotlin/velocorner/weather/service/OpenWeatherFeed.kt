package velocorner.weather.service

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import velocorner.weather.model.CurrentWeatherResponse
import velocorner.weather.model.ForecastWeatherResponse

class OpenWeatherFeed {

    private val baseUrl = "https://api.openweathermap.org/data/2.5"
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val apiKey = System.getenv("WEATHER_API_KEY").also {
        logger.info("OpenWeatherMap key is [${it.takeLast(4).padStart(it.length, 'X')}]")
    }

    suspend fun current(location: String): CurrentWeatherResponse? =
        get<CurrentWeatherResponse>("weather", location)

    suspend fun forecast(location: String): ForecastWeatherResponse? =
        get<ForecastWeatherResponse>("forecast", location)

    internal suspend inline fun <reified T> get(path: String, location: String): T? {
        HttpClient(Java).use {
            val response = it.get("$baseUrl/$path") {
                parameter("q", location)
                parameter("appid", apiKey)
                parameter("units", "metric")
                parameter("lang", "en")
            }
            return runCatching { json.decodeFromString<T>(response.bodyAsText()) }.getOrNull()
        }
    }
}
