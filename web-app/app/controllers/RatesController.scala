package controllers

import cats.data.OptionT
import controllers.util.WebMetrics
import play.api.libs.json.{JsNumber, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import velocorner.feed.{ExchangeRatesFeed, RatesFeed}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RatesController @Inject() (val connectivity: ConnectivitySettings, components: ControllerComponents)
    extends AbstractController(components)
    with WebMetrics {

  val feed: RatesFeed = new ExchangeRatesFeed(connectivity.secretConfig)

  // route mapped to /api/rates
  def rates(base: String, counter: String): Action[AnyContent] =
    Action.async {
      timedRequest[AnyContent](s"convert $base/$counter") { _ =>
        val res = for {
          mc <- OptionT.liftF(feed.moneyContext())
          baseCcy <- OptionT(Future(ExchangeRatesFeed.supported.get(base)))
          counterCcy <- OptionT(Future(ExchangeRatesFeed.supported.get(counter)))
        } yield baseCcy(1).to(counterCcy)(mc)
        res.map(rate => Ok(JsNumber(rate))).getOrElse(NotFound)
      }
    }
}
