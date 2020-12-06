package controllers.auth;

import org.specs2.mutable.Specification

class JwtUserSpec extends Specification {

  "JWT user" should {
    "encode" in {
      val user = JwtUser(
        name = "Rider",
        location = "Veloland",
        avatarUrl = "avatarUrl"
      )
      val token = user.toToken
      val claim = JwtUser.fromToken(token)
      user === claim
    }
  }
}
