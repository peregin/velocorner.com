package controllers

import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.{FakeRequest, Helpers, StubControllerComponentsFactory}
import squants.market.{CHF, CurrencyExchangeRate, MoneyContext, USD}
import velocorner.feed.{HUF, RatesFeed}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RatesControllerSpec extends PlaySpec with StubControllerComponentsFactory with MockitoSugar {

  "rest controller for exchange rates" should {

    val settingsMock = mock[ConnectivitySettings]
    val controller = new RatesController(settingsMock, stubControllerComponents()) {
      override lazy val feed: RatesFeed = new RatesFeed {
        override def moneyContext(): Future[MoneyContext] = Future(
          MoneyContext(
            CHF,
            Set(USD, HUF),
            Seq(
              CurrencyExchangeRate(CHF(1), HUF(400)),
              CurrencyExchangeRate(CHF(1), USD(1.5))
            )
          )
        )
      }
    }

    "retrieve CHF/HUF rate" in {
      val result = controller.rates("CHF", "HUF").apply(FakeRequest())
      Helpers.status(result) mustBe Status.OK
      val rate = Helpers.contentAsJson(result).as[Double]
      rate mustBe 400d
    }

    "retrieve chf/usd rate" in {
      val result = controller.rates("chf", "usd").apply(FakeRequest())
      Helpers.status(result) mustBe Status.OK
      val rate = Helpers.contentAsJson(result).as[Double]
      rate mustBe 1.5d
    }

    "respond with not found for unknown currencies" in {
      val result = controller.rates("EUR", "USD").apply(FakeRequest())
      Helpers.status(result) mustBe Status.NOT_FOUND
    }
  }
}
