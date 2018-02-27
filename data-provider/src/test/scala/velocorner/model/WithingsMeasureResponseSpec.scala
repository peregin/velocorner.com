package velocorner.model

import org.joda.time.DateTime
import org.specs2.mutable.Specification
import velocorner.util.JsonIo

import scala.io.Source

class WithingsMeasureResponseSpec extends Specification {

  "Withings response" should {

    val json = Source.fromURL(getClass.getResource("/data/withings/measures.json")).mkString
    val measures = JsonIo.read[WithingsMeasureResponse](json)

    "be loaded from reference file" in {
      measures.status === 0
      measures.body.updatetime.compareTo(DateTime.parse("2018-01-07T20:38:43.000+01:00")) === 0
      measures.body.timezone === "Europe/Zurich"
      measures.body.measuregrps must haveSize(49)
      val first = measures.body.measuregrps.head
      first.grpid === 999543286
      first.attrib === 0
      first.date.compareTo(DateTime.parse("2018-01-05T23:16:56.000+01:00")) === 0
      first.category === 1
      first.measures must haveSize(1)
      val entry = first.measures.head
      entry.value === 76567
      entry.`type` === 1
      entry.unit === -3
    }

    "be converted in internal model" in {
      val measurements = measures.convert(101)
      measurements must haveSize(97)
      val first = measurements.head
      first.id === "999543286_1"
      first.`type` === MeasurementType.Weight
      first.value must beCloseTo(76.567, .001)
      first.athleteId === 101
      first.updateTime.compareTo(DateTime.parse("2018-01-05T23:16:56.000+01:00")) === 0
    }
  }
}
