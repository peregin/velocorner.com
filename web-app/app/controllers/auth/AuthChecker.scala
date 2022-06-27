package controllers.auth

import StravaController.{ec, Id, ResultUpdater, User}
import controllers.ConnectivitySettings
import controllers.auth.AuthChecker.{OAuth2AttrKey, OAuth2CookieKey}
import play.api.cache.SyncCacheApi
import play.api.mvc._
import velocorner.model.Account

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import controllers.util.WebMetrics
import play.api.libs.typedmap.TypedKey

object AuthChecker {

  val OAuth2CookieKey = "velocorner.oauth2.id"
  val OAuth2AttrKey = TypedKey[Account]
}

// supports authentication based on:
// - session cookie
// - JWT headers
trait AuthChecker extends WebMetrics {
  // because of the body parser
  this: AbstractController =>

  val cache: SyncCacheApi

  val connectivity: ConnectivitySettings

  val sessionTimeoutInSeconds: Int = (7 days).toSeconds.toInt

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

  class AuthActionBuilder[B](p: BodyParser[B]) extends ActionBuilder[Request, B] {

    override protected def executionContext: ExecutionContext = ec
    override def parser: BodyParser[B] = p

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

  def AuthAsyncAction[B](p: BodyParser[B])(f: Request[B] => Future[Result]): Action[B] = new AuthActionBuilder(p).async(f)
  def TimedAuthAsyncAction[B](text: String)(p: BodyParser[B])(f: Request[B] => Future[Result]): Action[B] = AuthAsyncAction(p)(
    timedRequest(text)(f)
  )

  def AuthAction[B](p: BodyParser[B])(f: Request[B] => Result): Action[B] = new AuthActionBuilder(p).apply(f)

  def loggedIn(implicit request: Request[_]): Option[Account] = request.attrs.get[Account](OAuth2AttrKey)

  private def restoreUser(implicit request: RequestHeader, context: ExecutionContext): Future[(Option[User], ResultUpdater)] =
    (for {
      token <- tokenAccessor.extract(request)
    } yield for {
      Some(userId) <- idContainer.get(token)
      Some(user) <- resolveUser(userId)
      _ <- idContainer.prolongTimeout(token, sessionTimeoutInSeconds)
    } yield Option(user) -> tokenAccessor.put(token) _) getOrElse {
      Future.successful(Option.empty -> identity)
    }
}
