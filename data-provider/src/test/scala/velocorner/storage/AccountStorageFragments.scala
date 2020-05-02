package velocorner.storage

import cats.implicits._
import org.joda.time.{DateTime, DateTimeZone}
import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment
import velocorner.manual.AwaitSupport
import velocorner.model.Account

import scala.concurrent.Future

trait AccountStorageFragments extends Specification with AwaitSupport {

  def accountFragments(storage: => Storage[Future]): Fragment = {

    lazy val accountStorage = storage.getAccountStorage
    val now = DateTime.parse("2020-05-02T20:33:20.000+02:00").withZone(DateTimeZone.forID("Europe/Zurich"))
    lazy val account = Account(1, "display name", "display location", "profile url", "token", now.some)

    "read empty for non existent account" in {
      awaitOn(accountStorage.getAccount(-1)) must beEmpty
    }

    "add account twice as upsert" in {
      awaitOn(accountStorage.store(account))
      awaitOn(accountStorage.store(account))
      val maybeAccount = awaitOn(accountStorage.getAccount(1))
      maybeAccount === account.some
    }
  }
}
