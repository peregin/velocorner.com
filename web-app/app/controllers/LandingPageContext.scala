package controllers

import highcharts.DailySeries
import velocorner.model.Account

/**
  * Created by levi on 04/11/15.
  */
case class LandingPageContext(account: Option[Account],
                              yearlyDistance: Iterable[DailySeries],
                              aggregatedYearlyDistance: Iterable[DailySeries],
                              aggregatedYearlyElevation: Iterable[DailySeries]
                              )
