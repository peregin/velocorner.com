package velocorner.storage

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.sql.SQLException
import java.util.concurrent.TimeUnit

class PsqlDbStorageLifecycleSpec extends AnyWordSpec with Matchers {

  "storage shutdown" should {
    "close an unused pool and executor without connecting, and allow repeated cleanup" in {
      val storage = new PsqlDbStorage("jdbc:postgresql://localhost:1/unused", "unused", "unused")
      storage.destroy()
      storage.destroy()

      storage.dataSource.isClosed mustBe true
      storage.connectExecutor.isShutdown mustBe true
      storage.connectExecutor.awaitTermination(5, TimeUnit.SECONDS) mustBe true
      intercept[SQLException](storage.dataSource.getConnection)
    }
  }
}
