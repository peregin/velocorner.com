package controllers

import controllers.util.WebMetrics
import play.api.mvc.{AbstractController, ControllerComponents}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class DemoController @Inject()(val connectivity: ConnectivitySettings, components: ControllerComponents)
  extends AbstractController(components) with WebMetrics {


  // demo ytd data
  // route mapped to /api/demo/statistics/ytd/:action
  def ytdStatistics(action: String)= Action.async { timedRequest(s"demo ytd data for $action") { implicit request =>
    Future(Ok)
  }}
}
