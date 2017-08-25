package velocorner.storage

import java.io.File

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal
import org.apache.commons.io.FileUtils
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import velocorner.model.Activity
import velocorner.util.JsonIo

import scala.io.Source

class OrientDbStorageSpec extends Specification with BeforeAfterAll {

  if (ODatabaseRecordThreadLocal.INSTANCE == null) {
    sys.error("Calling this manually apparently prevent an initialization issue.")
  }

  sequential
  stopOnFail

  @volatile var storage: OrientDbStorage = null

  "storage" should {

    "check that is empty" in {
      storage.dailyProgressForAll(10) must beEmpty
    }

    "add items" in {
      val json = Source.fromURL(getClass.getResource("/data/strava/last30activities.json")).mkString
      val activities = JsonIo.read[List[Activity]](json).filter(_.`type` == "Ride")
      storage.store(activities)
      storage.listRecentActivities(50) must haveSize(24)
    }

    "retrieve daily stats" in {
      storage.dailyProgressForAll(50) must haveSize(15)
    }
  }

  override def beforeAll() {
    storage = new OrientDbStorage("orientdb_data_test", "memory")
    storage.initialize()
  }

  override def afterAll() {
    val dir = storage.rootDir
    storage.destroy()
    storage = null
    FileUtils.deleteDirectory(new File(dir))
  }
}
