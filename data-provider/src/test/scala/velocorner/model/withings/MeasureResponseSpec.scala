package velocorner.model.withings

import org.joda.time.DateTime
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import velocorner.util.JsonIo

class MeasureResponseSpec extends AnyWordSpec with Matchers {

  "Withings response" should {

    val measures = JsonIo.readReadFromResource[MeasureResponse]("/data/withings/measures.json")

    "be loaded from reference file" in {
      measures.status === 0
      measures.body.updatetime.compareTo(DateTime.parse("2018-01-07T20:38:43.000+01:00")) === 0
      measures.body.timezone === "Europe/Zurich"
      measures.body.measuregrps must have size 49
      val first = measures.body.measuregrps.head
      first.grpid === 999543286L
      first.attrib === 0
      first.date.compareTo(DateTime.parse("2018-01-05T23:16:56.000+01:00")) === 0
      first.category === 1
      first.measures must have size 1
      val entry = first.measures.head
      entry.value === 76567
      entry.`type` === 1
      entry.unit === -3
    }

    "be converted in internal model" in {
      val measurements = measures.convert(101)
      measurements must have size 97
      val first = measurements.head
      first.id === "999543286_1"
      first.`type` === MeasurementType.Weight
      first.value mustBe 76.567 +- .001
      first.athleteId === 101
      first.updateTime.compareTo(DateTime.parse("2018-01-05T23:16:56.000+01:00")) === 0
    }
  }
}
