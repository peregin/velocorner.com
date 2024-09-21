package velocorner.weather.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import io.ktor.server.html.*
import kotlinx.html.*

val javaOpts = System.getenv("JAVA_OPTS") ?: "n/a"

fun Route.welcomeRoutes() {
    get("/") {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)
        call.respondHtml(HttpStatusCode.OK) {
            head {
                title("Weather Service")
                link(rel = "icon", href = "/favicon.ico", type = "image/x-icon")
                link(rel = "icon", href = "/favicon-16x16.png", type = "image/png")
                link(rel = "icon", href = "/favicon-32x32.png", type = "image/png")
            }
            body {
                h1 { +"Welcome @ $now" }
                ul {
                    li { a("weather/current/Zurich,CH") { +"current weather for Zürich, Switzerland 🇨🇭" } }
                    li { a("weather/forecast/Zurich,CH") { +"5 days forecast ☀️ in 🇨🇭" } }
                }
                p { +"JAVA_OPTS: $javaOpts" }
            }
        }
    }
}