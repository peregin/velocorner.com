package controllers

import org.slf4s
import org.slf4s.Logging
import play.Logger
import play.api.mvc.Controller

/**
  * Created by levi on 10/12/15.
  */
trait PlayLogging extends Logging {
  self: Controller =>

  override val log = new slf4s.Logger(Logger.underlying())
}
