package model

import org.specs2.mutable.Specification
import play.api.Environment
import velocorner.util.JsonIo

class StatusInfoSpec extends Specification {

  "model" should {

    val status = StatusInfo.create(Environment.simple().mode)

    "read and write from json" in {
      val json = JsonIo.write(status)
      val statusRead = JsonIo.read[StatusInfo](json)
      statusRead === status
    }

    "marshal status" in {
      val json = JsonIo.write(status)
      json must contain(""""type" : "Status"""".stripMargin)
    }
  }
}
