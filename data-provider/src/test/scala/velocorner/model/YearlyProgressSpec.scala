package velocorner.model

import org.joda.time.LocalDate
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import velocorner.api.Progress

/**
 * Created by levi on 09/07/15.
 */
class YearlyProgressSpec extends AnyWordSpec with Matchers {

  private val today = LocalDate.parse("2015-07-10")
  private val progress = Progress(1, 1, 10, 10, 1000, 3, 30, 30)

  "model" should {

    val threeDayProgress = List(
      DailyProgress(today, progress),
      DailyProgress(today.plusDays(1), progress),
      DailyProgress(today.plusDays(2), progress)
    )
    val yp = YearlyProgress(2015, threeDayProgress)

    "aggregate previous items" in {
      val ap = YearlyProgress.aggregate(List(yp))
      ap must have size 1
      val adp = ap.head.progress
      adp must have size 3
      adp.head.progress.rides === 1
      adp.drop(1).head.progress.rides === 2
      adp.drop(2).head.progress.rides === 3
    }

    "fill in with zero progress the missing dates" in {
      val ypWithZeros = yp.zeroOnMissingDate
      ypWithZeros.progress must have size 365
      ypWithZeros.progress must contain(DailyProgress(today.withDayOfMonth(1).withMonthOfYear(1), Progress.zero))
    }

    "filter year to date progress" in {
      // drops all dates after today
      val ytd = yp.ytd(today)
      ytd.progress must have size 1
      ytd.progress must contain theSameElementsAs List(DailyProgress(today, progress))
    }
  }
}
