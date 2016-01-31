package velocorner.model

import org.specs2.mutable.Specification

/**
  * Created by levi on 31/01/16.
  */
class AthleteDailyProgressSpec extends Specification {

  "parser" should {
    "extract date and id" in {
      val progress = AthleteDailyProgress.fromStorageByDateId("[[2016,1,25],432909]",
        "{\"ride\":1,\"dist\":32496.099609375,\"distmax\":32496.099609375,\"elev\":0,\"elevmax\":0,\"time\":6170}")
      progress.athleteId === 432909
      progress.dailyProgress.getDay === 25
      progress.dailyProgress.getMonth === 0
    }
  }


}
