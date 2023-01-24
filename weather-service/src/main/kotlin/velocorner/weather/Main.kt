package velocorner.weather

import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
//    configureRouting()
    configureSerialization()
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}