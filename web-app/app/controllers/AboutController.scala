package controllers

import javax.inject.Inject

import play.api.mvc._

class AboutController @Inject()(components: ControllerComponents)(implicit assets: AssetsFinder)
  extends AbstractController(components) {

  def about = Action {
    Ok(views.html.about(assets))
  }
}
