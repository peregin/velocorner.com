package velocorner.model

import org.joda.time.DateTime
import org.specs2.mutable.Specification
import velocorner.util.JsonIo

import scala.io.Source

class MeasuresSpec extends Specification {

  "model" should {

    "be loaded from reference file" in {

      val json = Source.fromURL(getClass.getResource("/data/withings/measures.json")).mkString
      val measures = JsonIo.read[MeasuresResponse](json)
      measures.status === 0
      measures.body.updatetime.compareTo(DateTime.parse("2018-01-07T20:38:43.000+01:00")) === 0
      measures.body.timezone === "Europe/Zurich"
      measures.body.measuregrps must haveSize(49)
      val first = measures.body.measuregrps.head
      first.grpid === 999543286
    }
  }
}
