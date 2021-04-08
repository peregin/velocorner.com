package velocorner.api

import org.joda.time.DateTime
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import velocorner.util.JsonIo

class AchievementSpec extends AnyWordSpec with Matchers {

  val now = DateTime.parse("2019-06-11T01:00:00.000+02:00")

  "converting model to json" should {
    "be idempotent" in {
      val achievement = new Achievement(
        value = 12.5d,
        activityId = 123L,
        activityName = "name",
        activityTime = now
      )
      val json = JsonIo.write(achievement)
      val read = JsonIo.read[Achievement](json)
      read.value === achievement.value
      read.activityId === achievement.activityId
    }
  }
}
