package velocorner.weather.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import velocorner.weather.model.Weather
import velocorner.weather.service.Controller

fun Route.weatherRoutes(apiKey: String) {
    val controller = Controller(apiKey)
    route("weather") {
        get("current/{location?}") {
            val location = call.parameters["location"] ?: return@get call.respondText(
                "Missing location",
                status = HttpStatusCode.BadRequest
            )
            controller.current(location)
            call.respond(listOf(Weather(1)))
        }
        get("forecast/{location?}") {
            val location = call.parameters["location"] ?: return@get call.respondText(
                "Missing location",
                status = HttpStatusCode.BadRequest
            )
            call.respond(listOf(Weather(2)))
        }
    }
}