package velocorner.weather.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import velocorner.weather.service.OpenWeatherFeed
import velocorner.weather.service.WeatherService

fun Route.weatherRoutes(feed: OpenWeatherFeed) {
    val service = WeatherService(feed)
    route("weather") {
        get("current/{location?}") {
            val location = call.parameters["location"] ?: return@get call.respondText(
                "Missing location",
                status = HttpStatusCode.BadRequest
            )
            val current = service.current(location) ?: return@get call.respondText(
                "Unknown location $location",
                status = HttpStatusCode.NotFound
            )
            call.respond(current)
        }
        get("forecast/{location?}") {
            val location = call.parameters["location"] ?: return@get call.respondText(
                "Missing location",
                status = HttpStatusCode.BadRequest
            )
            call.respond(HttpStatusCode.NotImplemented, "coming soon") //listOf(Weather(1)))
        }
    }
}