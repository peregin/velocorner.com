package velocorner.weather.repo

import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.junit.Before
import velocorner.weather.model.CurrentWeather
import velocorner.weather.model.CurrentWeatherResponse
import velocorner.weather.model.ForecastWeather
import velocorner.weather.model.ForecastWeatherResponse
import velocorner.weather.repo.DatabaseFactory.transact
import velocorner.weather.util.WeatherCodeUtil
import kotlin.test.*
import kotlin.test.Test

internal class WeatherRepoTest {

    val zhLocation = "Zurich,CH"
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private inline fun <reified T> load(resource: String): T =
        json.decodeFromString<T>(this.javaClass.getResource(resource).readText())

    private val currentFixture = load<CurrentWeatherResponse>("/current.json")
    private val forecastFixture = load<ForecastWeatherResponse>("/forecast.json")

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
        truncate()
    }

    private fun truncate() = runBlocking {
        transact {
            CurrentWeatherTable.deleteAll()
            ForecastWeatherTable.deleteAll()
        }
    }

    @Test
    fun emptyCurrentForUnknownLocation() = runBlocking {
        val repo = WeatherRepoImpl()
        assertEquals(null, repo.getCurrent("unknown, loc"))
        assertEquals(null, repo.getCurrent("Budapest"))
        assertEquals(null, repo.getCurrent("Zurich"))
    }

    @Test
    fun upsertCurrentWeather() = runBlocking {
        val repo = WeatherRepoImpl()
        val weather = CurrentWeather(
            location = zhLocation,
            timestamp = currentFixture.dt!!,
            bootstrapIcon = WeatherCodeUtil.bootstrapIcon(currentFixture.weather!!.first().id),
            current = currentFixture.weather!!.first(),
            info = currentFixture.main!!,
            sunriseSunset = currentFixture.sys!!,
            coord = currentFixture.coord!!
        )
        repo.storeCurrent(weather)
        assertEquals(weather, repo.getCurrent(zhLocation))
        // store it again, we should have only one entry
        repo.storeCurrent(weather)
        val entries = transact { CurrentWeatherTable.selectAll().count() }
        assertEquals(1, entries)
    }

    @Test
    fun emptyForecastForUnknownLocation() = runBlocking {
        val repo = WeatherRepoImpl()
        val entries = repo.listForecast("unknown, loc")
        assertEquals(0, entries.size)
    }

    // idempotent
    @Test fun upsertForecastWeather() = runBlocking {
        val repo = WeatherRepoImpl()
        assertEquals(40, forecastFixture.list?.size!!)
        repo.storeForecast(forecastFixture.list!!.map{ e -> ForecastWeather(
            location = zhLocation,
            timestamp = e.dt,
            forecast = e) }
        )

        assertEquals(40, repo.listForecast(zhLocation).size)
        assertEquals(0, repo.listForecast("Budapest,HU").size)

        // storing entries are idempotent (upsert the same entries, we should have still 40 items in the storage)
        val first = forecastFixture.list!!.first()
        repo.storeForecast(listOf(ForecastWeather(zhLocation, first.dt, first)))
        assertEquals(40, repo.listForecast(zhLocation, limit = 50).size)

        // different location, same timestamp
        repo.storeForecast(listOf(ForecastWeather("Budapest,HU", first.dt, first)))
        assertEquals(40, repo.listForecast(zhLocation, limit = 50).size)
        assertEquals(1, repo.listForecast("Budapest,HU", limit = 50).size)
    }
}