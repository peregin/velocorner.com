package controllers.auth

import StravaController.AuthenticityToken

import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.RequestHeader

trait AsyncIdContainer[Id] {

  def startNewSession(userId: Id, timeoutInSeconds: Int)(implicit context: ExecutionContext): Future[AuthenticityToken]

  def remove(token: AuthenticityToken)(implicit context: ExecutionContext): Future[Unit]
  def get(token: AuthenticityToken)(implicit context: ExecutionContext): Future[Option[Id]]

  def prolongTimeout(token: AuthenticityToken, timeoutInSeconds: Int)(implicit
      request: RequestHeader,
      context: ExecutionContext
  ): Future[Unit]

}
object AsyncIdContainer {
  // generic here, IdContainer
  def apply[A](underlying: CacheIdContainer[A]): AsyncIdContainer[A] = new AsyncIdContainer[A] {
    import Future.{successful => future}
    def startNewSession(userId: A, timeoutInSeconds: Int)(implicit context: ExecutionContext): Future[AuthenticityToken] =
      future(underlying.startNewSession(userId, timeoutInSeconds))
    def remove(token: AuthenticityToken)(implicit context: ExecutionContext): Future[Unit] = future(underlying.remove(token))
    def get(token: AuthenticityToken)(implicit context: ExecutionContext): Future[Option[A]] = future(underlying.get(token))
    def prolongTimeout(token: AuthenticityToken, timeoutInSeconds: Int)(implicit
        request: RequestHeader,
        context: ExecutionContext
    ): Future[Unit] =
      future(underlying.prolongTimeout(token, timeoutInSeconds))
  }
}
