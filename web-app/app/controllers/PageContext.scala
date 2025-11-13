package controllers

import velocorner.api.Account

/**
 * Created by levi on 04/11/15.
 */
case class PageContext(
    title: String,
    account: Option[Account],
    isWithingsEnabled: Boolean,
    isWindyEnabled: Boolean,
    windyApiKey: String,
    isCrawlerEnabled: Boolean
) {

  def isLoggedIn(): Boolean = account.isDefined

  def isAdmin(): Boolean = account.exists(_.isAdmin())
}
