package velocorner.weather

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.welcomeRoutes() {
    get("/") {
        call.respondText("Aloha!", ContentType.Text.Html)
    }
}