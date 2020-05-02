package velocorner.storage

import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment
import velocorner.api.Activity
import velocorner.manual.AwaitSupport
import velocorner.model.DailyProgress

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait ActivityStorageFragments extends Specification with AwaitSupport {

  def activityFragments(storage: => Storage[Future], activityFixtures: List[Activity]): Fragment = {

    "be empty at startup" in {
      awaitOn(storage.listAllActivities(432909, "Ride")) must beEmpty
    }

    "add activity twice as upsert" in {
      val single = activityFixtures.headOption.toList
      awaitOn(storage.storeActivity(single))
      awaitOn(storage.storeActivity(single))
      awaitOn(storage.listAllActivities(432909, "Ride")) must haveSize(1)
    }

    "add items as idempotent operation" in {
      awaitOn(storage.storeActivity(activityFixtures))
      awaitOn(storage.listRecentActivities(432909, 50)) must haveSize(30)

      // is it idempotent
      awaitOn(storage.storeActivity(activityFixtures))
      awaitOn(storage.listRecentActivities(432909, 50)) must haveSize(30)
    }

    "retrieve recent activities for an athlete" in {
      awaitOn(storage.listRecentActivities(432909, 50)) must haveSize(30)
    }

    "retrieve daily stats for an athlete" in {
      awaitOn(storage.listAllActivities(432909, "Ride").map(DailyProgress.from)) must haveSize(15)
      awaitOn(storage.listAllActivities(432909, "Hike")) must haveSize(1)
      awaitOn(storage.listAllActivities(432909, "AlpineSki")) must haveSize(2)
    }

    "suggest activities for a specific athlete" in {
      val activities = awaitOn(storage.suggestActivities("Stallikon", 432909, 10))
      activities must haveSize(3)
      implicit val ordered = new Ordering[Activity] {
        override def compare(x: Activity, y: Activity): Int = y.start_date.compareTo(x.start_date)
      }
      activities.toList must beSorted
    }

    "suggest no activities when athletes are not specified" in {
      val activities = awaitOn(storage.suggestActivities("Stallikon", 1, 10))
      activities must beEmpty
    }

    "suggest activities case insensitive" in {
      val activities = awaitOn(storage.suggestActivities("stAlLIkon", 432909, 10))
      activities must haveSize(3)
    }

    "suggest activities with a partial search pattern" in {
      val activities = awaitOn(storage.suggestActivities("alli", 432909, 10))
      activities must haveSize(3)
    }

    "retrieve existing activity" in {
      awaitOn(storage.getActivity(244993130)).map(_.id) should beSome(244993130L)
      val skiing = awaitOn(storage.getActivity(240142636)).getOrElse(sys.error("not found"))
      skiing.`type` === "AlpineSki"
    }

    "return empty on non existent activity" in {
      awaitOn(storage.getActivity(111)) must beNone
    }

    "list activity types" in {
      awaitOn(storage.listActivityTypes(432909)) should containTheSameElementsAs(Seq("Ride", "Run", "AlpineSki", "Hike"))
    }
  }
}
