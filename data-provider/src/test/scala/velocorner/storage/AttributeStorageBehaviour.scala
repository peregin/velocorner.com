package velocorner.storage

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import velocorner.manual.AwaitSupport

import scala.concurrent.Future

trait AttributeStorageBehaviour extends Matchers with AwaitSupport { this: AnyFlatSpec =>

  def attributeFragments(storage: => Storage[Future]): Unit = {

    lazy val attributeStorage = storage.getAttributeStorage

    it should "store/lookup attributes" in {
      awaitOn(attributeStorage.getAttribute("key", "test")) mustBe empty

      awaitOn(attributeStorage.storeAttribute("key", "test", "value"))
      awaitOn(attributeStorage.getAttribute("key", "test")) mustBe Some("value")

      awaitOn(attributeStorage.getAttribute("key", "test2")) mustBe empty

      awaitOn(attributeStorage.storeAttribute("key", "test", "value2"))
      awaitOn(attributeStorage.getAttribute("key", "test")) mustBe Some("value2")
    }
  }
}
