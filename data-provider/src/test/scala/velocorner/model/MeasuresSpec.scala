package velocorner.model

import org.specs2.mutable.Specification
import velocorner.util.JsonIo

import scala.io.Source

class MeasuresSpec extends Specification {

  "model" should {

    "be loaded from reference file" in {

      val json = Source.fromURL(getClass.getResource("/data/withings/measures.json")).mkString
      val measures = JsonIo.read[MeasuresResponse](json)
      measures.status === 0
      measures.body.timezone === "Europe/Zurich"
    }
  }
}
