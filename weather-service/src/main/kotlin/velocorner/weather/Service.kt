package velocorner.weather

import com.typesafe.config.ConfigFactory
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import velocorner.weather.route.*

fun main() {
    embeddedServer(Netty, port = 9015, host = "0.0.0.0") {
        log.info("loading config...")
        val config = ConfigFactory.load()
        val apiKey = config.getString("weather.application.id")
        log.info("OpenWeatherApi key is [${apiKey.takeLast(4).padStart(apiKey.length, 'X')}]")
        install(ContentNegotiation) {
            json()
        }
        routing {
            welcomeRoutes()
            weatherRoutes()
        }
    }.start(wait = true)
}