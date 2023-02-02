package velocorner.weather.route

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import velocorner.weather.model.Weather
import velocorner.weather.service.Controller

fun Route.weatherRoutes(apiKey: String) {
    val controller = Controller(apiKey)
    route("current") {
        get {
            call.respond(listOf(Weather(1)))
        }
    }
}