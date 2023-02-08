package velocorner.weather.repo

import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.selectAll
import org.junit.Before
import velocorner.weather.model.CurrentWeather
import velocorner.weather.model.CurrentWeatherResponse
import velocorner.weather.repo.DatabaseFactory.transact
import velocorner.weather.util.WeatherCodeUtil
import kotlin.test.*
import kotlin.test.Test

internal class WeatherRepoTest {

    val zhLocation = "Zurich,CH"
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val weatherFixture = json.decodeFromString<CurrentWeatherResponse>(this.javaClass.getResource("/current.json").readText())

    @Before
    fun setup() {
        val config = ConfigFactory.parseString(
            """
            weather.psql.url="jdbc:postgresql://localhost:5492/integration_test"
            psql.user="velocorner"
            psql.password="velocorner"
        """.trimIndent()
        )
        DatabaseFactory.init(config)
    }

    @Test fun upsertCurrentWeather() = runBlocking {
        val repo = WeatherRepoImpl()
        val weather = CurrentWeather(
            location = zhLocation,
            timestamp = weatherFixture.dt!!,
            bootstrapIcon = WeatherCodeUtil.bootstrapIcon(weatherFixture.weather!!.first().id),
            current = weatherFixture.weather!!.first(),
            info = weatherFixture.main!!,
            sunriseSunset = weatherFixture.sys!!,
            coord = weatherFixture.coord!!
        )
        assertEquals(null, repo.getCurrent("Budapest"))
        repo.storeCurrent(weather)
        assertEquals(weather, repo.getCurrent(zhLocation))
        assertEquals(null, repo.getCurrent("Zurich"))
        assertEquals(null, repo.getCurrent("New York"))
        // store it again, we should have only one entry
        repo.storeCurrent(weather)
        val entries = transact { CurrentEntries.selectAll().count() }
        assertEquals(1, entries)
    }
}