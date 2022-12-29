package controllers

import mouse.all.booleanSyntaxMouse
import controllers.util.WebMetrics
import play.api.libs.json.{JsArray, JsString, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import velocorner.feed.{ProductCrawlerFeed, ProductFeed}
import velocorner.util.JsonIo

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ProductsController @Inject() (val connectivity: ConnectivitySettings, components: ControllerComponents)
    extends AbstractController(components)
    with WebMetrics {

  val feed: ProductFeed = new ProductCrawlerFeed(connectivity.secretConfig)

  // wip - from elastic or live from marketplaces
  // route mapped to /api/products/suggest
  def suggest(query: String): Action[AnyContent] =
    Action { _ =>
      logger.info(s"suggest products for $query")
      Ok(Json.obj("suggestions" -> Json.arr(JsString("not implemented yet..."))))
    }

  // route mapped to /api/products/search
  def search(query: String): Action[AnyContent] =
    Action.async {
      timedRequest[AnyContent](s"search products for [$query]") { _ =>
        if (query.isBlank) Future(Ok(JsArray()))
        else
          for {
            products <- feed.search(query.trim)
          } yield Ok(JsonIo.write(products))
      }
    }

  // route mapped to /api/products/markets
  def markets(): Action[AnyContent] =
    Action.async {
      for {
        products <- feed.supported()
      } yield Ok(JsonIo.write(products))
    }
}
