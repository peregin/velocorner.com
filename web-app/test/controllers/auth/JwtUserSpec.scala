package controllers.auth;

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec


class JwtUserSpec extends AnyWordSpec with Matchers with LazyLogging {

  implicit val secret = "secret"

  "JWT user" should {

    "encode" in {
      val user = JwtUser(
        id = 1L,
        name = "Rider",
        location = "Veloland",
        avatarUrl = "avatarUrl"
      )
      val token = user.toToken
      info(s"jwt token is: $token")
      val claim = JwtUser.fromToken(token)
      user === claim
    }
  }
}
