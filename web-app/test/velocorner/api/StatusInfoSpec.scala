package velocorner.api

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Environment
import velocorner.util.JsonIo

class StatusInfoSpec extends AnyWordSpec with Matchers {

  "model" should {

    val status = StatusInfo.compute(Environment.simple().mode, pings = 2)

    "read and write from json" in {
      val json = JsonIo.write(status)
      val statusRead = JsonIo.read[StatusInfo](json)
      statusRead === status
    }

    "marshal status" in {
      val json = JsonIo.write(status)
      json must include(""""type" : "Status"""".stripMargin)
      json must include(""""buildTime" :""".stripMargin)
    }
  }
}
