package velocorner.weather.repo

import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import velocorner.weather.model.CurrentWeather
import velocorner.weather.model.ForecastWeather
import velocorner.weather.repo.DatabaseFactory.transact

interface WeatherRepo {
    // weather - location is <city[,countryISO2letter]>
    // limit for 5 day forecast broken down to 3 hours = 8 entries/day and 40 entries/5 days
    suspend fun listForecast(location: String, limit: Int = 40): List<ForecastWeather>

    suspend fun storeForecast(forecast: List<ForecastWeather>)

    suspend fun getCurrent(location: String): CurrentWeather?

    suspend fun storeCurrent(weather: CurrentWeather)
}

object CurrentEntries : Table("weather") {
    val location = varchar("location", 256)
    val updateTime = datetime("update_time")
    val data = json<CurrentWeather>("data", Json)

    override val primaryKey = PrimaryKey(location)
}

class WeatherRepoImpl : WeatherRepo {

    override suspend fun listForecast(location: String, limit: Int): List<ForecastWeather> = transact {
        TODO("not yet")
    }

    override suspend fun storeForecast(forecast: List<ForecastWeather>) {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrent(location: String): CurrentWeather? = transact {
        CurrentEntries
            .select { CurrentEntries.location eq location }
            .map { it[CurrentEntries.data] }
            .singleOrNull()
    }

    override suspend fun storeCurrent(weather: CurrentWeather) {
        transact {
            val count = CurrentEntries.insertIgnore {
                it[location] = weather.location
                it[updateTime] = weather.timestamp.toLocalDateTime().toKotlinLocalDateTime()
                it[data] = weather
            }.insertedCount
            if (count == 0) {
                CurrentEntries.update({ CurrentEntries.location eq weather.location }) {
                    it[updateTime] = weather.timestamp.toLocalDateTime().toKotlinLocalDateTime()
                    it[data] = weather
                }
            }
        }
    }
}
