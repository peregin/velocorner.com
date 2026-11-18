package controllers

import controllers.util.WebMetrics
import play.api.{Environment, Mode}
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.{JsArray, JsString, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Request}
import squants.market.USD
import velocorner.feed.{ExchangeRatesFeed, ProductCrawlerFeed, WeatherLocationFeed}
import velocorner.util.{CountryUtils, JsonIo}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.math.BigDecimal.RoundingMode

class ProductsController @Inject() (
    val connectivity: ConnectivitySettings,
    environment: Environment,
    components: ControllerComponents,
    lifecycle: ApplicationLifecycle
)
    extends AbstractController(components)
    with WebMetrics {

  private[controllers] val productFeed = new ProductCrawlerFeed(connectivity.secretConfig, connectivity.materializer)
  private[controllers] val ratesFeed = new ExchangeRatesFeed(connectivity.secretConfig, connectivity.materializer)
  private[controllers] val weatherFeed = new WeatherLocationFeed(connectivity.secretConfig, connectivity.materializer)

  List(productFeed, ratesFeed, weatherFeed).foreach(feed => lifecycle.addStopHook(() => Future(feed.close())))

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
          for {
            products <- productFeed.search(query.trim)
            // detect country of ip, use weather service -> location/ip endpoint
            ip = detectIp(request, environment)
            countryCode2 <- weatherFeed.countryLocation(ip)
            // detect currency of the country
            detectedCcy = CountryUtils.code2Currency.getOrElse(countryCode2, "USD")
            baseCcy = ExchangeRatesFeed.supported.getOrElse(detectedCcy, USD)
            mc <- ratesFeed.moneyContext()
            // convert prices into the base currency
            productsInBaseCcy = products.map { pd =>
              val price = pd.price.toSquants(using mc).to(baseCcy)(using mc).setScale(2, RoundingMode.HALF_EVEN)
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

  def detectIp(request: Request[AnyContent], environment: Environment): String = {
    val remoteAddress = request.headers.get("X-Forwarded-For").getOrElse(request.remoteAddress) match {
      case "0:0:0:0:0:0:0:1" if environment.mode == Mode.Dev => "85.1.45.35"
      case "0:0:0:0:0:0:0:1"                                 => "127.0.0.1"
      case other                                             => other
    }
    logger.debug(s"remote address is $remoteAddress")
    remoteAddress
  }
}
