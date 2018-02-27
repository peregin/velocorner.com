package velocorner.search

import java.nio.file.Files

import com.sksamuel.elastic4s.embedded.LocalNode
import com.sksamuel.elastic4s.http.ElasticDsl._
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

    val localNode = LocalNode("mycluster", path.toString)
    val client = localNode.http(true)

    "create indices" in {
      val json = Source.fromURL(getClass.getResource("/data/strava/last30activities.json")).mkString
      val activities = JsonIo.read[List[Activity]](json)
      val indices = map2Indices(activities)
      val maybeStatus = client.execute(bulk(indices)).await.right.toOption.map(_.status)
      maybeStatus must beSome(200)
    }

    step {
      client.close()
      val file = path.toFile
      if (file.exists()) file.delete()
    }
  }
}
