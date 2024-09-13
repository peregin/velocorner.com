package velocorner.weather.repo

import com.typesafe.config.Config
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(config: Config, driverClassName: String = "org.postgresql.Driver") {
        val dbUrl = config.getString("weather.psql.url")
        // fallback to old user if config was not updated
        val dbUser = if (config.hasPath("weather.psql.user")) config.getString("weather.psql.user") else config.getString("psql.user")
        val dbPassword = config.getString("psql.password")
        val dataSource = hikari(dbUrl, dbUser, dbPassword, driverClassName)
        Database.connect(dataSource)
        val flyway = Flyway.configure().locations("psql/migration").dataSource(dataSource).load()
        flyway.migrate()
    }

    private fun hikari(dbUrl: String, dbUser: String, dbPassword: String, driverClassName: String): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = driverClassName
        config.jdbcUrl = dbUrl
        config.username = dbUser
        config.password = dbPassword
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> transact(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction { block() }
        }
}
