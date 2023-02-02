package velocorner.weather.route

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import velocorner.weather.model.Weather

fun Route.weatherRoutes() {
    route("current") {
        get {
            call.respond(listOf(Weather(1)))
        }
    }
}