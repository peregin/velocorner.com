package controllers

import highcharts.DailySeries
import velocorner.model.{Account, Progress}

/**
  * Created by levi on 04/11/15.
  */
case class LandingPageContext(account: Option[Account],
                              statistics: Progress,
                              yearlyProgress: Iterable[DailySeries],
                              aggregatedYearlyProgress: Iterable[DailySeries],
                              recentDistance: Iterable[DailySeries],
                              recentElevation: Iterable[DailySeries]
                              )
