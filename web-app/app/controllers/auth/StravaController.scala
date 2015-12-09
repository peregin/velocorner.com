package controllers.auth

import jp.t2v.lab.play2.auth.social.core.{OAuth2Authenticator, OAuth2Controller}
import jp.t2v.lab.play2.auth.{AuthConfig, Login, OptionalAuthElement}

/**
  * Created by levi on 09/12/15.
  */
trait StravaController extends OAuth2Controller with AuthConfig with OptionalAuthElement with Login {

  override protected val authenticator: OAuth2Authenticator = new StravaAuthenticator

  def callback(scope: Option[String]) = link(scope.getOrElse("public"))
}