package controllers

import controllers.util.{RemoteIp, WebMetrics}
import play.api.Environment
import play.api.libs.json.{JsArray, JsString, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import squants.market.USD
import velocorner.feed.{ExchangeRatesFeed, ProductCrawlerFeed, ProductFeed, RatesFeed}
import velocorner.util.{CountryUtils, JsonIo}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.math.BigDecimal.RoundingMode

class ProductsController @Inject() (val connectivity: ConnectivitySettings, environment: Environment, components: ControllerComponents)
    extends AbstractController(components)
    with RemoteIp
    with WebMetrics {

  val productFeed: ProductFeed = new ProductCrawlerFeed(connectivity.secretConfig)
  val ratesFeed: RatesFeed = new ExchangeRatesFeed(connectivity.secretConfig)

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
      timedRequest[AnyContent](s"search products for [$query]") { request =>
        if (query.isBlank) Future(Ok(JsArray()))
        else {
          val remoteIp = detectIp(request, environment)
          for {
            products <- productFeed.search(query.trim)
            // detect country of ip
            countryCode2 <- connectivity.getStorage.getLocationStorage.getCountry(remoteIp).map(_.getOrElse("US"))
            // detect currency of the country
            detectedCcy = CountryUtils.code2Currency.getOrElse(countryCode2, "USD")
            baseCcy = ExchangeRatesFeed.supported.getOrElse(detectedCcy, USD)
            mc <- ratesFeed.moneyContext()
            // convert prices into the base currency
            productsInBaseCcy = products.map { pd =>
              val price = pd.price.toSquants(mc).to(baseCcy)(mc).setScale(2, RoundingMode.HALF_EVEN)
              pd.copy(price = velocorner.api.Money.fromSquants(baseCcy.apply(price)))
            }
          } yield Ok(JsonIo.write(productsInBaseCcy))
        }
      }
    }

  // route mapped to /api/products/markets
  def markets(): Action[AnyContent] =
    Action.async {
      for {
        products <- productFeed.supported()
      } yield Ok(JsonIo.write(products))
    }
}
