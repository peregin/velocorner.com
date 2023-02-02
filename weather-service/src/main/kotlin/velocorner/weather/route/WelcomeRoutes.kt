package velocorner.weather.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Route.welcomeRoutes() {
    get("/") {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)
        call.respondText("Welcome @ $now", ContentType.Text.Html)
    }
}