package velocorner.storage

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.typesafe.scalalogging.LazyLogging
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import velocorner.api.Activity
import velocorner.util.JsonIo

class PsqlDbStorageSpec extends Specification with BeforeAfterAll with ActivityStorageFragment with LazyLogging {

  sequential
  stopOnFail

  @volatile var psql: EmbeddedPostgres = _
  @volatile var psqlStorage: PsqlDbStorage = _

  "pqsl storage" should {

    val activityFixtures = JsonIo
      .readReadFromResource[List[Activity]]("/data/strava/last30activities.json")
      .filter(_.`type` == "Ride")

    addFragmentsBlock(activityFragments(psqlStorage, activityFixtures))
  }

  override def beforeAll(): Unit = {
    logger.info("starting embedded psql...")
    try {
      psql = EmbeddedPostgres.builder()
        .setLocaleConfig("locale", "en_US") // without won't work from IntelliJ, injects different locale
        .setLocaleConfig("lc-messages", "en_US")
        .start()
      val port = psql.getPort
      psqlStorage = new PsqlDbStorage(dbUrl = s"jdbc:postgresql://localhost:$port/postgres", dbUser = "postgres", dbPassword = "test")
      psqlStorage.initialize()
    } catch {
      case any: Exception =>
        logger.error("failed to start embedded psql", any)
    }
  }

  override def afterAll(): Unit = {
    psqlStorage.destroy()
    psql.close()
  }
}
