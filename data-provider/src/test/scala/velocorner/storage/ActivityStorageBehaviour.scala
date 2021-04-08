package velocorner.storage

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import velocorner.api.strava.Activity
import velocorner.manual.AwaitSupport
import velocorner.model.DailyProgress

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ActivityStorageBehaviour extends Matchers with AwaitSupport { this: AnyFlatSpec =>

  def activityFragments(storage: => Storage[Future], activityFixtures: List[Activity]): Unit = {

    it should "be empty at startup" in {
      awaitOn(storage.listAllActivities(432909, "Ride")) mustBe empty
    }

    it should "add activity twice as upsert" in {
      val single = activityFixtures.headOption.toList
      awaitOn(storage.storeActivity(single))
      awaitOn(storage.storeActivity(single))
      awaitOn(storage.listAllActivities(432909, "Ride")) must have size 1
    }

    it should "add items as idempotent operation" in {
      awaitOn(storage.storeActivity(activityFixtures))
      awaitOn(storage.listRecentActivities(432909, 50)) must have size 30

      // is it idempotent
      awaitOn(storage.storeActivity(activityFixtures))
      awaitOn(storage.listRecentActivities(432909, 50)) must have size 30
    }

    it should "retrieve recent activities for an athlete" in {
      awaitOn(storage.listRecentActivities(432909, 50)) must have size 30
    }

    it should "retrieve daily stats for an athlete" in {
      awaitOn(storage.listAllActivities(432909, "Ride").map(DailyProgress.from)) must have size 15
      awaitOn(storage.listAllActivities(432909, "Hike")) must have size 1
      awaitOn(storage.listAllActivities(432909, "AlpineSki")) must have size 2
    }

    it should "suggest activities for a specific athlete" in {
      val activities = awaitOn(storage.suggestActivities("Stallikon", 432909, 10))
      activities must have size 3
      implicit val ordered = new Ordering[Activity] {
        override def compare(x: Activity, y: Activity): Int = y.start_date.compareTo(x.start_date)
      }
      activities.toList mustBe sorted
    }

    it should "suggest no activities when athletes are not specified" in {
      val activities = awaitOn(storage.suggestActivities("Stallikon", 1, 10))
      activities mustBe empty
    }

    it should "suggest activities case insensitive" in {
      val activities = awaitOn(storage.suggestActivities("stAlLIkon", 432909, 10))
      activities must have size 3
    }

    it should "suggest activities with a partial search pattern" in {
      val activities = awaitOn(storage.suggestActivities("alli", 432909, 10))
      activities must have size 3
    }

    it should "retrieve existing activity" in {
      awaitOn(storage.getActivity(244993130)).map(_.id) mustBe Some(244993130L)
      val skiing = awaitOn(storage.getActivity(240142636)).getOrElse(sys.error("not found"))
      skiing.`type` === "AlpineSki"
    }

    it should "return empty on non existent activity" in {
      awaitOn(storage.getActivity(111)) mustBe empty
    }

    it should "list activity types" in {
      awaitOn(storage.listActivityTypes(432909)) must contain theSameElementsAs Seq("Ride", "Run", "AlpineSki", "Hike")
    }
  }
}
