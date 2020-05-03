package velocorner.storage

import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import velocorner.api.Activity
import velocorner.api.weather.{SunriseSunset, WeatherForecast}
import velocorner.model.{Account, KeyValue}
import velocorner.util.Metrics

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MigrateOrient2Psql(orient: OrientDbStorage, psql: PsqlDbStorage) extends LazyLogging with Metrics {

  def doIt(): Future[Unit] = {
    for {
      accounts <- timedFuture("query accounts")(orient.queryFor[Account](s"SELECT FROM ${OrientDbStorage.ACCOUNT_CLASS}"))
      _ = logger.info(s"found ${accounts.size} accounts in orient")
      psqlAccounts <- psql.toFuture(sql"select count(*) from account".query[Int].unique)
      _ = logger.info(s"found $psqlAccounts accounts in psql")

      forecasts <- timedFuture("query forecasts")(orient.queryFor[WeatherForecast](s"SELECT FROM ${OrientDbStorage.WEATHER_CLASS}"))
      _ = logger.info(s"found ${forecasts.size} forecasts")

      suns <- timedFuture("query suns")(orient.queryFor[SunriseSunset](s"SELECT FROM ${OrientDbStorage.SUN_CLASS}"))
      _ = logger.info(s"found ${suns.size} suns")

      attributes <- timedFuture("query attributes")(orient.queryFor[KeyValue](s"SELECT FROM ${OrientDbStorage.ATTRIBUTE_CLASS}"))
      _ = logger.info(s"found ${attributes.size} attributes")

      activities <- timedFuture("query activities")(orient.queryFor[Activity](s"SELECT FROM ${OrientDbStorage.ACTIVITY_CLASS}"))
      _ = logger.info(s"found ${activities.size} activities")
    } yield ()
  }

}
