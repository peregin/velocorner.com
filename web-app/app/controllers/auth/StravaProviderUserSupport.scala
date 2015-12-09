package controllers.auth

import jp.t2v.lab.play2.auth.social.core.{OAuth2Controller, OAuthProviderUserSupport}
import velocorner.model.Account

import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by levi on 09/12/15.
  */
trait StravaProviderUserSupport extends OAuthProviderUserSupport {
  self: OAuth2Controller =>

  override type ProviderUser = Account

  override def retrieveProviderUser(accessToken: AccessToken)(implicit ctx: ExecutionContext): Future[ProviderUser] = {
    ???
  }
}
