package velocorner.feed

import org.joda.time.DateTime
import velocorner.api.{Account, OAuth2Access}
import velocorner.model.strava.Athlete

object OAuth2 {

  type AccessToken = String
  type RefreshToken = String
  type ProviderUser = Athlete
  type ConsumerUser = Account

  case class OAuth2TokenResponse(
      accessToken: AccessToken,
      expiresAt: DateTime,
      refreshToken: RefreshToken,
      providerUser: Option[ProviderUser]
  ) {

    def toStravaAccess = OAuth2Access(
      accessToken = accessToken,
      accessExpiresAt = expiresAt,
      refreshToken = refreshToken
    )
  }
}
