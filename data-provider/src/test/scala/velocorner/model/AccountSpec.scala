package velocorner.model

import velocorner.util.JsonIo
import cats.implicits._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** Created by levi on 01/12/15.
  */
class AccountSpec extends AnyWordSpec with Matchers with AccountFixtures {

  "model" should {

    "add type to the json" in {
      val json = JsonIo.write(account)
      json mustEqual """{
          |  "athleteId" : 1,
          |  "displayName" : "display name",
          |  "displayLocation" : "display location",
          |  "avatarUrl" : "profile url",
          |  "unit" : "imperial",
          |  "stravaAccess" : {
          |    "accessToken" : "accessToken",
          |    "accessExpiresAt" : "2020-05-02T20:33:20Z",
          |    "refreshToken" : "refreshToken"
          |  },
          |  "type" : "Account"
          |}""".stripMargin
    }

    "serialize with role and units" in {
      val accountWithRole = account.copy(
        lastUpdate = None,
        role = Role.Admin.some,
        unit = Units.Metric.some,
        stravaAccess = None
      )
      val json = JsonIo.write(accountWithRole)
      val ref = JsonIo.read[Account](json)
      ref.role === Role.Admin.some
      ref.unit === Units.Metric.some
    }
  }
}
