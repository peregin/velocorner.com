package controllers

import jp.t2v.lab.play2.auth.AuthConfig
import velocorner.model.{Permission, Account}

import scala.reflect._

/**
  * Created by levi on 30/11/15.
  */
trait AppAuthConfig extends AuthConfig {

  type Id = Long

  type User = Account

  type Authority = Permission

  val idTag: ClassTag[Id] = classTag[Id]
  val sessionTimeoutInSeconds: Int = 3600

}
