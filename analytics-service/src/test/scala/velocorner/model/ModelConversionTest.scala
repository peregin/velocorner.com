package velocorner.model

import argonaut._
import argonaut.Argonaut._
import org.scalatest.{FlatSpec, Matchers}

class ModelConversionTest extends FlatSpec with Matchers {

  case class TestResult(validLines: Int, invalidLines: Int)

  it should "convert model to json" in {
    val test = TestResult(3, 4)
    implicit val codec = CodecJson.derive[TestResult]
    val json = test.asJson.spaces2
    json shouldBe
      """{
        |  "validLines" : 3,
        |  "invalidLines" : 4
        |}""".stripMargin
  }
}
