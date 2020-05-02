package velocorner.storage

import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment
import velocorner.manual.AwaitSupport

import scala.concurrent.Future

trait AttributeStorageFragments extends Specification with AwaitSupport {

  def attributeFragments(storage: => Storage[Future]): Fragment = {

    lazy val attributeStorage = storage.getAttributeStorage

    "store/lookup attributes" in {
      awaitOn(attributeStorage.getAttribute("key", "test")) must beNone

      awaitOn(attributeStorage.storeAttribute("key", "test", "value"))
      awaitOn(attributeStorage.getAttribute("key", "test")) must beSome("value")

      awaitOn(attributeStorage.getAttribute("key", "test2")) must beNone

      awaitOn(attributeStorage.storeAttribute("key", "test", "value2"))
      awaitOn(attributeStorage.getAttribute("key", "test")) must beSome("value2")
    }
  }
}
