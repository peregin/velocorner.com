package velocorner.storage

import cats.implicits._
import org.joda.time.{DateTime, DateTimeZone}
import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment
import velocorner.manual.AwaitSupport
import velocorner.model.{Account, OAuth2Access}

import scala.concurrent.Future

trait AccountStorageFragments extends Specification with AwaitSupport {

  def accountFragments(storage: => Storage[Future]): Fragment = {

    lazy val accountStorage = storage.getAccountStorage
    val now = DateTime.parse("2020-05-02T20:33:20.000+02:00").withZone(DateTimeZone.UTC)
    lazy val account = Account(1, "display name", "display location", "profile url", lastUpdate = now.some, None,
      OAuth2Access("accessToken", now.plusHours(6), "refreshToken").some)

    "read empty for non existent account" in {
      awaitOn(accountStorage.getAccount(-1)) must beEmpty
    }

    "add account twice as upsert" in {
      awaitOn(accountStorage.store(account))
      awaitOn(accountStorage.store(account))
      val maybeAccount = awaitOn(accountStorage.getAccount(1))
      val dbAccount = maybeAccount.getOrElse(sys.error("not found"))
      // without lastUpdate - has different timezone locally and on TravisCI
      dbAccount.copy(lastUpdate = None) === account.copy(lastUpdate = None)
      // check the local date part only
      dbAccount.lastUpdate.getOrElse(sys.error("not found")).toLocalDate === account.lastUpdate.getOrElse(sys.error("not found")).toLocalDate
    }
  }
}
