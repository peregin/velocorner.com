package controllers.auth;

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class JwtUserSpec extends AnyWordSpec with Matchers {

  "JWT user" should {
    "encode" in {
      val user = JwtUser(
        name = "Rider",
        location = "Veloland",
        avatarUrl = "avatarUrl"
      )
      val token = user.toToken()
      val claim = JwtUser.fromToken(token)
      user === claim
    }
  }
}
