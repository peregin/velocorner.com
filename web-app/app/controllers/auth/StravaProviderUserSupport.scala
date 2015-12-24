package controllers.auth

import controllers.Global
import jp.t2v.lab.play2.auth.social.core.{OAuth2Controller, OAuthProviderUserSupport}
import velocorner.model.Account

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by levi on 09/12/15.
  */
trait StravaProviderUserSupport extends OAuthProviderUserSupport {
  self: OAuth2Controller =>

  override type ProviderUser = Account

  override def retrieveProviderUser(accessToken: AccessToken)(implicit ctx: ExecutionContext): Future[ProviderUser] = {
    val token = accessToken.toString
    val athlete = Global.getFeed(token).getAthlete
    Future.successful(Account.from(athlete, token, None))
  }
}
