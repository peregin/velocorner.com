package velocorner.storage

import java.io.File

import org.apache.commons.io.FileUtils
import org.slf4s.Logging
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import velocorner.model.Activity
import velocorner.util.{FreePortFinder, JsonIo}

import scala.io.Source

class OrientDbStorageSpec extends Specification with BeforeAfterAll with Logging {

  sequential
  stopOnFail

  @volatile var storage: OrientDbStorage = _

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

    "retrieve recent activities for an athlete" in {
      storage.listRecentActivities(432909, 50) must haveSize(24)
    }

    "retrieve daily stats for an athlete" in {
      storage.dailyProgressForAthlete(432909) must haveSize(15)
    }

    "suggest activities for a specific athlete" in {
      val activities = storage.suggestActivities("Stallikon", 432909, 10)
      activities must haveSize(3)
    }

    "suggest no activities when athletes are not specified" in {
      val activities = storage.suggestActivities("Stallikon", 1, 10)
      activities must beEmpty
    }

    "suggest activities case insensitive" in {
      val activities = storage.suggestActivities("stAlLIkon", 432909, 10)
      activities must haveSize(3)
    }

    "retrieve existing activity" in {
      val activity = storage.getActivity(244993130).getOrElse(sys.error("not found"))
      activity.id === 244993130
    }

    "return empty on non existent activity" in {
      storage.getActivity(111) must beNone
    }

    "backup the database" in {
      val file = File.createTempFile("orientdb", "backup")
      storage.backup(file.getAbsolutePath)
      file.length() must beGreaterThan(10L)
      file.delete()
    }
  }

  override def beforeAll() {
    // eventually the port is already used if the application runs locally
    val serverPort = FreePortFinder.find()
    log.info(s"running OrientDb on port $serverPort")
    storage = new OrientDbStorage("orientdb_data_test", MemoryStorage, serverPort)
    FileUtils.deleteDirectory(new File(storage.rootDir)) // cleanup previous incomplete test remainders
    storage.initialize()
  }

  override def afterAll() {
    storage.destroy()
    FileUtils.deleteDirectory(new File(storage.rootDir))
    storage = null
  }
}
