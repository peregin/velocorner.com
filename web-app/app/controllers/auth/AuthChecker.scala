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
import velocorner.util.Metrics

trait AuthChecker extends Metrics {
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
    connectivity.getStorage.getAccount(id)
  }

  class AuthActionBuilder extends ActionBuilder[Request, AnyContent] {

    override protected def executionContext: ExecutionContext = ec
    override def parser: BodyParser[AnyContent] = parse.default
    override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
      implicit val r = request
      val maybeUserFuture = restoreUser.recover { case _ => None -> identity[Result] _ }
      maybeUserFuture.flatMap { case (maybeUser, cookieUpdater) =>
        val richReq = maybeUser.map(u => request.addAttr(OAuth2AttrKey, u)).getOrElse(request)
        val eventualResult = block(richReq)
        eventualResult.map(cookieUpdater)
      }
    }
  }

  def AuthAsyncAction(f: Request[AnyContent] => Future[Result]): Action[AnyContent] = new AuthActionBuilder().async(f)
  def TimedAuthAsyncAction(text: String)(f: Request[AnyContent] => Future[Result]): Action[AnyContent] = AuthAsyncAction{
    val mark = System.currentTimeMillis()
    f.andThen{ g =>
      g.onComplete{ _ =>
        val elapsed = System.currentTimeMillis() - mark
        logger.info(s"$text took $elapsed millis")}
      g
    }
  }
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
