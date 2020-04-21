package velocorner.storage

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll

class PsqlDbStorageSpec extends Specification with BeforeAfterAll {

  sequential
  stopOnFail

  @volatile var psql: EmbeddedPostgres = _
  @volatile var storage: PsqlDbStorage = _

  override def beforeAll(): Unit = {
    psql = EmbeddedPostgres.start()
    val port = psql.getPort
    storage = new PsqlDbStorage(s"jdbc:postgresql://localhost:$port/velocorner", "test")
    storage.initialize()
  }

  override def afterAll(): Unit = {
    storage.destroy()
    psql.close()
  }
}
