package controllers

import play.api.mvc._
import velocorner.util.Metrics

object Application extends Controller with Metrics {

  def index = Action {

    val progress = timed("getting yearly progress")(Global.getDataHandler.yearlyProgress)
    Ok(views.html.index(progress))
  }

  def about = Action {
    Ok(views.html.about())
  }

}