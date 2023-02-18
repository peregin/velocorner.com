package velocorner.weather.route

import kotlin.test.Test
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.assertContains
import kotlin.test.assertEquals

internal class WelcomeRouteTest {

    @Test
    fun testWelcome() = testApplication {
        routing {
            welcomeRoutes()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ContentType.Text.Html.contentType, response.contentType()!!.contentType)
        val text = response.bodyAsText()
        assertContains(text, "Welcome @")
        assertContains(text, "<li><a href=\"weather/current/Zurich,CH\">current weather for Zürich, Switzerland 🇨🇭</a></li>")
    }
}