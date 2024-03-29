package controllers

import controllers.util.WebMetrics
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import velocorner.search.BrandSearch
import velocorner.util.JsonIo

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

class BrandsController @Inject() (val connectivity: ConnectivitySettings, components: ControllerComponents)
    extends AbstractController(components)
    with WebMetrics {

  val feed = new BrandSearch(connectivity.secretConfig)

  // route mapped to /api/brands/suggest
  def suggest(query: String): Action[AnyContent] =
    Action.async {
      timedRequest[AnyContent](s"suggest brand for $query") { _ =>
        for {
          brands <- feed.suggestBrands(query)
          jsonBrands = brands.map(b => Json.obj("value" -> b, "data" -> b))
        } yield Ok(Json.obj("suggestions" -> jsonBrands))
      }
    }

  // route mapped to /api/brands/search
  def search(query: String): Action[AnyContent] =
    Action.async {
      timedRequest[AnyContent](s"search brand for $query") { _ =>
        for {
          brands <- feed.searchBrands(query)
        } yield Ok(JsonIo.write(brands))
      }
    }
}
