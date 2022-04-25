package controllers

import controllers.util.WebMetrics
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import velocorner.search.MarketplaceElasticSupport
import velocorner.util.JsonIo

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

class BrandController @Inject() (val connectivity: ConnectivitySettings, components: ControllerComponents)
    extends AbstractController(components)
    with MarketplaceElasticSupport
    with WebMetrics {

  override def elasticUrl(): String = connectivity.getElasticUrl()

  // route mapped to /api/brand/suggest
  def suggest(query: String): Action[AnyContent] =
    Action.async {
      timedRequest(s"suggest brand for $query") { implicit request =>
        for {
          brands <- suggestBrands(query)
          jsonBrands = brands.map(b => Json.obj("value" -> b, "data" -> b))
        } yield Ok(Json.obj("suggestions" -> jsonBrands))
      }
    }

  // route mapped to /api/brand/search
  def search(query: String): Action[AnyContent] =
    Action.async {
      timedRequest(s"search brand for $query") { implicit request =>
        for {
          brands <- searchBrands(query)
        } yield Ok(JsonIo.write(brands))
      }
    }
}
