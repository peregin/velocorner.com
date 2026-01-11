package controllers.util

import play.api.mvc.{AnyContent, Request, Result}
import velocorner.util.Metrics
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait WebMetrics extends Metrics {

  type RequestResponseFun[B] = Request[B] => Future[Result]

  def timedRequest[B](text: String)(fun: RequestResponseFun[B]): RequestResponseFun[B] = {
    val mark = System.currentTimeMillis()
    fun.andThen { g =>
      g.onComplete { _ =>
        val elapsed = System.currentTimeMillis() - mark
        logger.info(s"$text took \u001b[33m$elapsed millis\u001b[0m")
      }
      g
    }
  }
}
