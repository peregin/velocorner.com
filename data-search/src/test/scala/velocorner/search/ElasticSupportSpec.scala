package velocorner.search

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.typesafe.scalalogging.LazyLogging
import org.specs2.mutable.Specification
import velocorner.model.strava.Activity
import velocorner.util.JsonIo


class ElasticSupportSpec extends Specification with ElasticSupport with LazyLogging {

  sequential

  // LocalNode is not supported anymore - SKIP TESTS HERE
  args(skipAll = true)

  "local cluster" should {

    val client = localCluster()

    "create indices" in {
      val activities = JsonIo.readReadFromResource[List[Activity]]("/data/strava/last30activities.json")
      activities must haveSize(30)
      val indices = toIndices(activities).map(_.refresh(RefreshPolicy.IMMEDIATE))
      val res = indices.map(ix => client.execute(ix).await)
      val statuses = res.map(_.status)
      statuses must haveSize(30) // it has 6 skiing events
      statuses must contain(201)
    }

    "search" in {
      val res = client.execute(search("activity") matchQuery("name", "Uetli") limit 5).await
      logger.info(s"found $res")

      res.status === 200
      val hits = res.result.hits.hits

      logger.info(s"search results ${hits.mkString(",")}")
      hits.length === 5
    }
  }
}
