package controllers

import jp.t2v.lab.play2.auth.AuthConfig
import org.slf4s.Logging
import play.api.mvc.Results._
import play.api.mvc.{Result, RequestHeader}
import velocorner.model.{Permission, Account}

import scala.concurrent.{Future, ExecutionContext}
import scala.reflect._

/**
  * Created by levi on 30/11/15.
  */
trait AuthConfigSupport extends AuthConfig with Logging {

  type Id = Long

  type User = Account

  type Authority = Permission

  val idTag: ClassTag[Id] = classTag[Id]
  val sessionTimeoutInSeconds: Int = 3600

  override def resolveUser(id: Long)(implicit context: ExecutionContext): Future[Option[Account]] = {
    log.info(s"resolving user[$id]")
    // TODO: find the user
    ???
  }

  override def loginSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Redirect(routes.Application.index()))
  }

  override def logoutSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Redirect(routes.Application.index()))
  }

  override def authorize(user: Account, authority: Permission)(implicit context: ExecutionContext): Future[Boolean] = {
    Future.successful(true)
  }

  override def authenticationFailed(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Redirect(routes.Application.index()))
  }


  override def authorizationFailed(request: RequestHeader, user: Account, authority: Option[Permission])(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Forbidden("no permission"))
  }
}
