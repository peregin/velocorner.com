package controllers.auth

import javax.inject.Inject
import controllers.ConnectivitySettings
import play.Logger
import play.api.libs.oauth.{ConsumerKey, OAuth, RequestToken, ServiceInfo}
import play.api.mvc._
import velocorner.ServiceProvider
import velocorner.api.Account

class WithingsController @Inject() (val connectivity: ConnectivitySettings, components: ControllerComponents) extends AbstractController(components) {

  val clientToken: String = connectivity.secretConfig.getAuthToken(ServiceProvider.Withings)
  val clientSecret: String = connectivity.secretConfig.getAuthSecret(ServiceProvider.Withings)
  val callbackUrl: String = connectivity.secretConfig.getAuthCallbackUrl(ServiceProvider.Withings)
  private val KEY = ConsumerKey(clientToken, clientSecret)

  private val oauth = OAuth(
    ServiceInfo(
      "https://developer.health.nokia.com/account/request_token",
      "https://developer.health.nokia.com/account/access_token",
      "https://developer.health.nokia.com/account/authorize",
      KEY
    ),
    use10a = true
  )

  private val logger = Logger.of(this.getClass)

  def login(scope: String): Action[AnyContent] = Action { implicit request =>
    loggedIn(request) match {
      case Some(a) =>
        Redirect(controllers.routes.WebController.index)
      case None =>
        process(request)
    }
  }

  def authorize: Action[AnyContent] = Action { implicit request =>
    logger.info(s"withings callback with $request request")
    process(request)
  }

  // FIXME: not implemented yet, re-use
  def loggedIn(implicit request: Request[AnyContent]): Option[Account] = None

  def sessionTokenPair(implicit request: RequestHeader): Option[RequestToken] =
    for {
      token <- request.session.get("token")
      secret <- request.session.get("secret")
    } yield RequestToken(token, secret)

  private def process(request: Request[AnyContent]): Result = {
    val res = request
      .getQueryString("oauth_verifier")
      .map { verifier => // means was from callback
        val tokenPair = sessionTokenPair(request).get
        // We got the verifier; now get the access token, store it and back to index
        oauth.retrieveAccessToken(tokenPair, verifier) match {
          case Right(t) =>
            // We received the authorized tokens in the OAuth object - store it before we proceed
            val maybeUserId = request.getQueryString("userid")
            logger.info(s"received authorization token $t and user identifier ${maybeUserId.mkString}")
            maybeUserId match {
              case Some(userId) =>
                Redirect(controllers.routes.WebController.index)
                  .withSession("token" -> t.token, "secret" -> t.secret, "withingsId" -> userId)
              case _ =>
                BadRequest
            }
          case Left(e) => throw e
        }
      }
      .getOrElse( // means it is triggered from the user
        oauth.retrieveRequestToken(callbackUrl) match {
          case Right(t) =>
            // We received the unauthorized tokens in the OAuth object - store it before we proceed
            logger.info(s"received request token $t")
            Redirect(oauth.redirectUrl(t.token)).withSession("token" -> t.token, "secret" -> t.secret)
          case Left(e) => throw e
        }
      )
    res
  }
}
