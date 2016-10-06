package controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

/**
  * Created by levi on 06/10/16.
  */
object RestController extends Controller {

  def recentClubDistance = Action.apply {
    Ok(Json.obj("message" -> "todo"))
  }
}
