package velocorner.model

import org.specs2.mutable.Specification
import velocorner.util.JsonIo

import cats.implicits._

/**
  * Created by levi on 01/12/15.
  */
class AccountSpec extends Specification {

  "model" should {

    "add type to the json" in {
      val account = Account(1, "display name", "display location", "profile url", "token", lastUpdate = None, role = None)
      val json = JsonIo.write(account)
      json ===
        """{
          |  "athleteId" : 1,
          |  "displayName" : "display name",
          |  "displayLocation" : "display location",
          |  "avatarUrl" : "profile url",
          |  "accessToken" : "token",
          |  "type" : "Account"
          |}""".stripMargin
    }

    "serialize with role" in {
      val account = Account(1, "name", "location", "profile", "token", lastUpdate = None, role = Role.Admin)
      val json = JsonIo.write(account)
      val ref = JsonIo.read[Account](json)
      ref.role === Role.Admin.some
    }
  }
}
