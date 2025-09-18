package controllers.auth

import StravaController.{ec, AuthenticityToken, Id, ResultUpdater, User}
import cats.data.OptionT
import cats.implicits.catsSyntaxOptionId
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

import scala.util.Try

object AuthChecker {

  private val OAuth2CookieKey = "velocorner.oauth2.id"
  private val OAuth2AttrKey = TypedKey[Account]
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
    cookieMaxAge = Some(sessionTimeoutInSeconds)
  )

  // auth conf
  private def resolveUser(id: Long): Future[Option[Account]] = {
    logger.info(s"resolving user[$id]")
    connectivity.getStorage.getAccountStorage.getAccount(id)
  }

  class AuthActionBuilder[B](p: BodyParser[B]) extends ActionBuilder[Request, B] {

    override protected def executionContext: ExecutionContext = ec
    override def parser: BodyParser[B] = p

    override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
      implicit val r: Request[A] = request
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

  // restore user from authorization bearer token or cookie
  private def restoreUser(implicit request: RequestHeader): Future[(Option[User], ResultUpdater)] = {
    def fromCookie(): OptionT[Future, (Long, ResultUpdater)] = for {
      token <- OptionT[Future, AuthenticityToken](Future.successful(tokenAccessor.extract(request)))
      userId <- OptionT(idContainer.get(token))
      _ <- OptionT.liftF(idContainer.prolongTimeout(token, sessionTimeoutInSeconds))
    } yield (userId, tokenAccessor.put(token) _)

    def fromBearer(): OptionT[Future, (Long, ResultUpdater)] = OptionT(Future.successful(for {
      token <- request.headers.get("Authorization").map(_.trim.stripPrefix("Bearer "))
      user <- Try(JwtUser.fromToken(token)(connectivity.secretConfig.getJwtSecret)).toOption
    } yield (user.id, identity _)))

    val maybeUserIdT = fromBearer() orElse fromCookie()
    (for {
      (userId, resultUpdater) <- maybeUserIdT
      user <- OptionT(resolveUser(userId))
    } yield (user, resultUpdater)).value.map {
      case Some((user, updater)) => (user.some, updater)
      case _                     => Option.empty -> identity
    }
  }
}
