package velocorner.storage

import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment
import velocorner.api.Activity
import velocorner.manual.AwaitSupport

import scala.concurrent.Future

trait ActivityStorageFragment extends Specification with AwaitSupport {

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
  }
}
