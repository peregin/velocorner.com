package controllers

import play.api.mvc._

class AboutController extends Controller {

  def about = Action {
    Ok(views.html.about())
  }
}
