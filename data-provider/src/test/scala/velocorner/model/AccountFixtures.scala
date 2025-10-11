package velocorner.model

import cats.implicits._
import org.joda.time.{DateTime, DateTimeZone}
import velocorner.api.{Account, OAuth2Access, Units}

trait AccountFixtures {

  val now = DateTime.parse("2020-05-02T20:33:20.000+02:00").withZone(DateTimeZone.forID("Europe/Zurich"))
  val access = OAuth2Access(
    "accessToken",
    accessExpiresAt = now,
    "refreshToken"
  )
  val account = Account(
    1,
    "display name",
    "display location",
    "profile url",
    lastUpdate = None,
    role = None,
    unit = Units.Imperial.some,
    stravaAccess = access.some
  )
}
