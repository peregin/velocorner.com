package controllers.auth

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import velocorner.model.AccountFixtures

class JwtUserSpec extends AnyWordSpec with Matchers with LazyLogging with AccountFixtures {

  implicit val secret: String = "secret"

  "JWT user" should {

    "encode raw data" in {
      val user = JwtUser(id = 1L)
      val token = user.toToken
      info(s"jwt token is: $token")
      val claim = JwtUser.fromToken(token)
      user === claim
    }

    "encode account" in {
      val user = JwtUser.toJwtUser(account)
      val token = user.toToken
      info(s"jwt token is: $token")
      val claim = JwtUser.fromToken(token)
      user === claim
    }
  }
}
