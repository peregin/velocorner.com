package controllers

import velocorner.model.Account

/**
  * Created by levi on 04/11/15.
  */
case class PageContext(account: Option[Account], isWithingsEnabled: Boolean)
