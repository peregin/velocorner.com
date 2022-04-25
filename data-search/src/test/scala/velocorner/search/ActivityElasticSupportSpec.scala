package velocorner.search

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import velocorner.api.strava.Activity
import velocorner.util.JsonIo

//noinspection TypeAnnotation
class ActivityElasticSupportSpec extends AnyFlatSpec with Matchers with ActivityElasticSupport with LazyLogging {

  val client = createElasticClient()

  override def elasticUrl(): String = "http://192.168.0.11:9200"

  // LocalNode is not supported anymore - SKIP TESTS HERE
  ignore should "create indices" in {
    val activities = JsonIo.readReadFromResource[List[Activity]]("/data/strava/last30activities.json")
    activities must have size 30
    val indices = toIndices(activities).map(_.refresh(RefreshPolicy.IMMEDIATE))
    val res = indices.map(ix => client.execute(ix).await)
    val statuses = res.map(_.status)
    statuses must have size 30 // it has 6 skiing events
    statuses must contain(201)
  }

  ignore should "search" in {
    val res = client.execute(search("activity") matchQuery ("name", "Uetli") limit 5).await
    logger.info(s"found $res")

    res.status === 200
    val hits = res.result.hits.hits

    logger.info(s"search results ${hits.mkString(",")}")
    hits.length === 5
  }
}
