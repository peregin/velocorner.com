package velocorner.weather

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 9015, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }
        routing {
            welcomeRoutes()
            userRouting()
        }
    }.start(wait = true)
}