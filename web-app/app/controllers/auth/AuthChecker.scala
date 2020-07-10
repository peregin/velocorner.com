package controllers.auth

import StravaController.{Id, OAuth2AttrKey, ResultUpdater, User, ec}
import controllers.ConnectivitySettings
import play.api.cache.SyncCacheApi
import play.api.mvc._
import velocorner.model.Account

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import StravaController.OAuth2CookieKey
import controllers.util.WebMetrics


trait AuthChecker extends WebMetrics {
  // because of the body parser
  this: AbstractController =>

  val cache: SyncCacheApi

  val connectivity: ConnectivitySettings

  val sessionTimeoutInSeconds: Int = (2 days).toSeconds.toInt

  // auth conf
  lazy val idContainer: AsyncIdContainer[Id] = AsyncIdContainer(new CacheIdContainer[Id](cache))

  // auth conf
  lazy val tokenAccessor = new CookieTokenAccessor(
    cookieName = OAuth2CookieKey,
    cookieSecureOption = false,
    cookieHttpOnlyOption = true,
    cookieDomainOption = None,
    cookiePathOption = "/",
    cookieMaxAge = Some(sessionTimeoutInSeconds)
  )

  // auth conf
  def resolveUser(id: Long)(implicit context: ExecutionContext): Future[Option[Account]] = {
    logger.info(s"resolving user[$id]")
    connectivity.getStorage.getAccountStorage.getAccount(id)
  }

  class AuthActionBuilder extends ActionBuilder[Request, AnyContent] {

    override protected def executionContext: ExecutionContext = ec
    override def parser: BodyParser[AnyContent] = parse.default
    override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
      implicit val r = request
      val maybeUserF = restoreUser.recover { case _ => None -> identity[Result] _ }
      maybeUserF.flatMap { case (maybeUser, cookieUpdater) =>
        val richReq = maybeUser.map(u => request.addAttr(OAuth2AttrKey, u)).getOrElse(request)
        val eventualResult = block(richReq)
        eventualResult.map(cookieUpdater)
      }
    }
  }

  def AuthAsyncAction(f: Request[AnyContent] => Future[Result]): Action[AnyContent] = new AuthActionBuilder().async(f)
  def TimedAuthAsyncAction(text: String)(f: Request[AnyContent] => Future[Result]): Action[AnyContent] = AuthAsyncAction(timedRequest(text)(f))

  def AuthAction(f: Request[AnyContent] => Result): Action[AnyContent] = new AuthActionBuilder().apply(f)

  def loggedIn(implicit request: Request[AnyContent]): Option[Account] = request.attrs.get[Account](OAuth2AttrKey)

  private def restoreUser(implicit request: RequestHeader, context: ExecutionContext): Future[(Option[User], ResultUpdater)] = {
    (for {
      token  <- tokenAccessor.extract(request)
    } yield for {
      Some(userId) <- idContainer.get(token)
      Some(user)   <- resolveUser(userId)
      _            <- idContainer.prolongTimeout(token, sessionTimeoutInSeconds)
    } yield {
      Option(user) -> tokenAccessor.put(token) _
    }) getOrElse {
      Future.successful(Option.empty -> identity)
    }
  }
}
