package controllers

import velocorner.model.Account

/**
  * Created by levi on 04/11/15.
  */
case class PageContext(title: String, account: Option[Account],
                       isWithingsEnabled: Boolean,
                       isWeatherEnabled: Boolean, weatherLocation: String) {

  def isLoggedIn(): Boolean = account.isDefined

  def isAdmin(): Boolean = account.exists(_.isAdmin())
}
