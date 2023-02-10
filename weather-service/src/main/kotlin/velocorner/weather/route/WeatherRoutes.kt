package velocorner.weather.route

import io.ktor.http.*
import io.ktor.http.ContentType.Application.Xml
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import velocorner.weather.model.ForecastWeather
import velocorner.weather.service.WeatherService
import velocorner.weather.util.toMeteoGramXml

// location is in format: city[,isoCountry 2 letter code]
fun Route.weatherRoutes(service: WeatherService) {
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
            val forecast = service.forecast(location)
            if (forecast.isEmpty()) return@get call.respondText(
                "Unknown location $location",
                status = HttpStatusCode.NotFound
            )
            call.respondText(toMeteoGramXml(forecast), contentType = Xml, status = HttpStatusCode.OK)
        }
    }
}
