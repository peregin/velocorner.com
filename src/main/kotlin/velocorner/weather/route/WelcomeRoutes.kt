package velocorner.weather.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import io.ktor.server.html.*
import kotlinx.html.*

fun Route.welcomeRoutes() {
    get("/") {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)
        call.respondHtml(HttpStatusCode.OK) {
            body {
                h1 { +"Welcome @ $now" }
                ul {
                    li { a("weather/current") { +"current weather" } }
                    li { a("weather/forecast") { +"5 days forecast" } }
                }
            }
        }
    }
}