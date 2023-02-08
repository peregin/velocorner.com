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

object CurrentWeatherTable : Table("weather") {
    val location = varchar("location", 64)
    val data = json<CurrentWeather>("data", Json)

    override val primaryKey = PrimaryKey(location)
}

object ForecastWeatherTable : Table("forecast") {
    val location = varchar("location", 64)
    val updateTime = datetime("update_time")
    val data = json<ForecastWeather>("data", Json)

    override val primaryKey = PrimaryKey(location, updateTime)
}

class WeatherRepoImpl : WeatherRepo {

    override suspend fun listForecast(location: String, limit: Int): List<ForecastWeather> = transact {
        ForecastWeatherTable
            .select { ForecastWeatherTable.location eq location }
            .orderBy(Pair(ForecastWeatherTable.updateTime, SortOrder.DESC))
            .limit(limit)
            .map { it[ForecastWeatherTable.data] }
    }

    override suspend fun storeForecast(forecast: List<ForecastWeather>) = transact {
        forecast.forEach { w ->
            val count = ForecastWeatherTable.insertIgnore {
                it[location] = w.location
                it[updateTime] = w.timestamp.toLocalDateTime().toKotlinLocalDateTime()
                it[data] = w
            }.insertedCount
            if (count == 0) {
                ForecastWeatherTable.update({
                    (ForecastWeatherTable.location eq w.location) and
                            (ForecastWeatherTable.updateTime eq w.timestamp.toLocalDateTime().toKotlinLocalDateTime())
                }) {
                    it[updateTime] = w.timestamp.toLocalDateTime().toKotlinLocalDateTime()
                    it[data] = w
                }
            }
        }
    }

    override suspend fun getCurrent(location: String): CurrentWeather? = transact {
        CurrentWeatherTable
            .select { CurrentWeatherTable.location eq location }
            .map { it[CurrentWeatherTable.data] }
            .singleOrNull()
    }

    override suspend fun storeCurrent(weather: CurrentWeather) {
        transact {
            val count = CurrentWeatherTable.insertIgnore {
                it[location] = weather.location
                it[data] = weather
            }.insertedCount
            if (count == 0) {
                CurrentWeatherTable.update({ CurrentWeatherTable.location eq weather.location }) {
                    it[data] = weather
                }
            }
        }
    }
}
