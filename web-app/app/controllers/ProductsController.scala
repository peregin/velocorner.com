package controllers

import controllers.util.WebMetrics
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import velocorner.feed.{ProductCrawlerFeed, ProductFeed}
import velocorner.search.BrandSearch
import velocorner.util.JsonIo

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

class ProductsController @Inject() (val connectivity: ConnectivitySettings, components: ControllerComponents)
    extends AbstractController(components)
    with WebMetrics {

  val feed: ProductFeed = new ProductCrawlerFeed(connectivity.secretConfig)

  // wip - from elastic or live from marketplaces
  // route mapped to /api/products/suggest
  def suggest(query: String): Action[AnyContent] =
    Action { _ =>
      logger.info(s"suggest products for $query")
      Ok(Json.obj("suggestions" -> Json.arr()))
    }

  // route mapped to /api/products/search
  def search(query: String): Action[AnyContent] =
    Action.async {
      timedRequest[AnyContent](s"search products for $query") { implicit request =>
        for {
          products <- feed.search(query)
        } yield Ok(JsonIo.write(products))
      }
    }
}
