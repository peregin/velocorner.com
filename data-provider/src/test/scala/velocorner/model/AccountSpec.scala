package velocorner.model

import org.specs2.mutable.Specification
import velocorner.util.JsonIo
import cats.implicits._
import org.joda.time.{DateTime, DateTimeZone}

/**
  * Created by levi on 01/12/15.
  */
class AccountSpec extends Specification {

  "model" should {

    "add type to the json" in {
      val now = DateTime.parse("2020-05-02T20:33:20.000+02:00").withZone(DateTimeZone.forID("Europe/Zurich"))
      val access = StravaAccess(
        "accessToken", accessExpiresAt = now, "refreshToken"
      )
      val account = Account(
        1, "display name", "display location", "profile url", lastUpdate = None, role = None,
        stravaAccess = access.some
      )
      val json = JsonIo.write(account)
      json ===
        """{
          |  "athleteId" : 1,
          |  "displayName" : "display name",
          |  "displayLocation" : "display location",
          |  "avatarUrl" : "profile url",
          |  "stravaAccessToken" : "accessToken",
          |  "type" : "Account"
          |}""".stripMargin
    }

    "serialize with role" in {
      val account = Account(1, "name", "location", "profile", lastUpdate = None, role = Role.Admin.some, None)
      val json = JsonIo.write(account)
      val ref = JsonIo.read[Account](json)
      ref.role === Role.Admin.some
    }
  }
}
