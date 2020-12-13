package velocorner.api

import org.specs2.mutable.Specification
import play.api.Environment
import velocorner.util.JsonIo

class StatusInfoSpec extends Specification {

  "model" should {

    val status = StatusInfo.compute(Environment.simple().mode, pings = 2)

    "read and write from json" in {
      val json = JsonIo.write(status)
      val statusRead = JsonIo.read[StatusInfo](json)
      statusRead === status
    }

    "marshal status" in {
      val json = JsonIo.write(status)
      json must contain(""""type" : "Status"""".stripMargin)
      json must contain(""""buildTime" :""".stripMargin)
    }
  }
}
