package controllers.auth

import controllers.AuthController.{Id, OAuth2AttrKey, User, ec}
import controllers.ConnectivitySettings
import jp.t2v.lab.play2.auth.{AsyncIdContainer, ResultUpdater}
import play.Logger
import play.api.cache.SyncCacheApi
import play.api.mvc._
import velocorner.model.Account

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait AuthChecker { //extends AuthConfigSupport {

  val cache: SyncCacheApi

  val connectivity: ConnectivitySettings

  val sessionTimeoutInSeconds: Int = (1 day).toSeconds.toInt

  // auth conf
  lazy val idContainer: AsyncIdContainer[Id] = AsyncIdContainer(new NewCacheIdContainer[Id](cache))

  // auth conf
  lazy val tokenAccessor = new NewCookieTokenAccessor(
    cookieName = "PLAY2AUTH_SESS_ID",
    cookieSecureOption = false,
    cookieHttpOnlyOption = true,
    cookieDomainOption = None,
    cookiePathOption = "/",
    cookieMaxAge = Some(sessionTimeoutInSeconds)
  )

  // auth conf
  def resolveUser(id: Long)(implicit context: ExecutionContext): Future[Option[Account]] = {
    Logger.info(s"resolving user[$id]")
    Future.successful(connectivity.getStorage.getAccount(id))
  }

  class AuthActionBuilder extends ActionBuilder[Request, AnyContent] {

    override protected def executionContext: ExecutionContext = ec
    override def parser: BodyParser[AnyContent] = BodyParsers.parse.default
    override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
      proceed(request)(block)
    }
  }

  def AuthAsyncAction(f: Request[AnyContent] => Future[Result]): Action[AnyContent] = new AuthActionBuilder().async(f)
  def AuthAction(f: Request[AnyContent] => Result): Action[AnyContent] = new AuthActionBuilder().apply(f)

  def loggedIn(implicit request: Request[AnyContent]): Option[Account] = request.attrs.get[Account](OAuth2AttrKey)

  private def extractToken(request: RequestHeader): Option[String] = tokenAccessor.extract(request)
  private def restoreUser(implicit request: RequestHeader, context: ExecutionContext): Future[(Option[User], ResultUpdater)] = {
    (for {
      token  <- extractToken(request)
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

  def proceed[A](req: Request[A])(f: Request[A] => Future[Result]): Future[Result] = {
    implicit val r = req
    val maybeUserFuture = restoreUser.recover { case _ => None -> identity[Result] _ }
    maybeUserFuture.flatMap { case (maybeUser, cookieUpdater) =>
      val richReq = maybeUser.map(u => req.addAttr(OAuth2AttrKey, u)).getOrElse(req)
      val rr = f(richReq)
      rr.map(cookieUpdater)
    }
  }

}
