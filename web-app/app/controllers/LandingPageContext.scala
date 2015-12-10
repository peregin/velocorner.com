package controllers

import velocorner.model.{Progress, YearlyProgress}

/**
  * Created by levi on 04/11/15.
  */
case class LandingPageContext(statistics: Progress,
                              yearlyProgress: List[YearlyProgress],
                              aggregatedYearlyProgress: List[YearlyProgress]
                              )
