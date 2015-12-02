package velocorner.model

import org.specs2.mutable.Specification
import velocorner.util.JsonIo

/**
  * Created by levi on 01/12/15.
  */
class AccountSpec extends Specification {

  "model" should {
    "add type to the json" in {
      val account = Account(1, "name", "profile url", "token")
      val json = JsonIo.write(account)
      json ===
        """{
          |  "athleteId" : 1,
          |  "name" : "name",
          |  "avatarUrl" : "profile url",
          |  "accessToken" : "token",
          |  "type" : "Account"
          |}""".stripMargin
    }
  }
}
