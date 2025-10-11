package velocorner.storage

import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import doobie.util.fragment.Fragment
import play.api.libs.json.Reads
import velocorner.api.Account
import velocorner.api.strava.Activity
import velocorner.util.Metrics

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//for kestrel combinator
import mouse.all._

class MigrateOrient2Psql(orient: OrientDbStorage, psql: PsqlDbStorage) extends LazyLogging with Metrics {

  def doIt(): Future[Unit] =
    for {
      _ <- migrateTable[Account](OrientDbStorage.ACCOUNT_CLASS, "account", e => e.traverse(psql.getAccountStorage.store).void)

      // _ <- migrateTable[WeatherForecast](OrientDbStorage.WEATHER_CLASS, "weather", e => psql.getWeatherStorage.storeRecentForecast(e))

      // _ <- migrateTable[SunriseSunset](OrientDbStorage.SUN_CLASS, "sun", e => e.traverse(psql.getWeatherStorage.storeRecentWeather).void)

      // _ <- migrateTable[KeyValue](
      //  OrientDbStorage.ATTRIBUTE_CLASS,
      //  "attribute",
      //  e => e.traverse(a => psql.getAttributeStorage.storeAttribute(a.key, a.`type`, a.value)).void
      // )

      _ <- migrateTable[Activity](OrientDbStorage.ACTIVITY_CLASS, "activity", e => psql.storeActivity(e))
    } yield ()

  protected def migrateTable[T: Reads](orientTable: String, psqlTable: String, psqlStore: List[T] => Future[Unit]): Future[Unit] = for {
    entries <- timedFuture(s"query $orientTable")(orient.queryFor[T](s"SELECT FROM $orientTable"))
    _ = logger.info(s"found ${entries.size} $orientTable in orient")
    psqlEntriesSize <- psql.toFuture((fr"select count(*) from " ++ Fragment.const(psqlTable)).query[Int].unique)
    _ = logger.info(s"found $psqlEntriesSize $psqlTable in psql")
    _ <-
      if (psqlEntriesSize > 0) Future.unit <| (_ => logger.info(s"no migration for $psqlTable"))
      else timedFuture(s"store $psqlTable")(psqlStore(entries.toList)) <| (_ => logger.info(s"migrated $orientTable!"))
    _ = logger.info("-----------------------------------------------")
  } yield ()

}
