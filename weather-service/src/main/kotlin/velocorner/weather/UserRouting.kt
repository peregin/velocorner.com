package velocorner.weather

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRouting() {
    route("users") {
        get {
            call.respond(listOf(User(1)))
        }
    }
}