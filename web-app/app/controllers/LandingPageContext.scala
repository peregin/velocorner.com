package controllers

import velocorner.model.{Account, Progress, YearlyProgress}

/**
  * Created by levi on 04/11/15.
  */
case class LandingPageContext(account: Option[Account],
                              statistics: Progress,
                              yearlyProgress: List[YearlyProgress],
                              aggregatedYearlyProgress: List[YearlyProgress]
                              )
