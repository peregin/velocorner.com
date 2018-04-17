package controllers

import javax.inject.Inject
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results._

import scala.concurrent.Future

class ErrorController @Inject()(implicit assets: AssetsFinder) extends DefaultHttpErrorHandler {

  override protected def onNotFound(request: RequestHeader, message: String): Future[Result] = {
    Future.successful(NotFound(views.html.notFound(assets = assets)))
  }
}
