package velocorner.storage

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import com.typesafe.scalalogging.LazyLogging
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import velocorner.api.Activity
import velocorner.util.JsonIo

class PsqlDbStorageSpec extends Specification with BeforeAfterAll with ActivityStorageFragments with AccountStorageFragments with LazyLogging {

  sequential
  stopOnFail

  // can't be executed as root - travis ci need a non root user
  @volatile var psql: EmbeddedPostgres = _
  @volatile var psqlStorage: PsqlDbStorage = _

  "pqsl storage" should {

    val activityFixtures = JsonIo.readReadFromResource[List[Activity]]("/data/strava/last30activities.json")

    addFragmentsBlock(activityFragments(psqlStorage, activityFixtures))

    addFragmentsBlock(accountFragments(psqlStorage))
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
