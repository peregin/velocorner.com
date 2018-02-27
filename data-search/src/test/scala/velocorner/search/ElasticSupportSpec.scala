package velocorner.search

import java.nio.file.Files

import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.embedded.LocalNode
import com.sksamuel.elastic4s.http.ElasticDsl._
import org.elasticsearch.common.settings.Settings
import org.slf4s.Logging
import org.specs2.mutable.Specification
import velocorner.model.Activity
import velocorner.util.JsonIo

import scala.io.Source

class ElasticSupportSpec extends Specification with ElasticSupport with Logging {

  sequential

  "local node" should {

    val path = Files.createTempDirectory("elastic")
    log.info(s"temporary workspace is $path")

    val localSettings = Settings.builder()
      .put("http.enabled", true)
      .put("processors", 1)
      .put("path.home", path.toString)
      .put("path.data", path.resolve("data").toString)
      .put("path.repo", path.resolve("repo").toString)
      .put("cluster.name", "test-velocorner")
    val localNode = LocalNode(localSettings.build())
    val client = localNode.http(true)

    "create indices" in {
      val json = Source.fromURL(getClass.getResource("/data/strava/last30activities.json")).mkString
      val activities = JsonIo.read[List[Activity]](json)
      val indices = map2Indices(activities).map(_.refresh(RefreshPolicy.IMMEDIATE))
      val res = client.execute(bulk(indices)).await
      val maybeStatus = res.right.toOption.map(_.status)
      maybeStatus must beSome(200)
    }

//    "search" in {
//      val res = client.execute(search("velocorner") query "Uetli" limit 5).await
//      log.info(s"found $res")
//      res match {
//        case Left(failure) => log.info(s"failed ${failure.error}")
//        case Right(results) => log.info(s"${results.result.hits}")
//      }
//      val maybeResult = res.right.toOption
//      maybeResult.map(_.status) must beSome(200)
//      maybeResult.map(_.result.hits.total) must beSome(5l)
//    }

    step {
      client.close()
      localNode.stop(true)
      val file = path.toFile
      if (file.exists()) file.delete()
    }
  }
}
