package controllers.util

import play.api.mvc.{AnyContent, Request, Result}
import velocorner.util.Metrics
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait WebMetrics extends Metrics {

  type RequestResponseFun = Request[AnyContent] => Future[Result]

  def timedRequest(text: String)(fun: RequestResponseFun): RequestResponseFun = {
    val mark = System.currentTimeMillis()
    fun.andThen{ g =>
      g.onComplete{ _ =>
        val elapsed = System.currentTimeMillis() - mark
        logger.info(s"$text took $elapsed millis")}
      g
    }
  }
}
