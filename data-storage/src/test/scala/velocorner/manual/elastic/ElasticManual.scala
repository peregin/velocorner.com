package velocorner.manual.elastic

import java.util.concurrent.CountDownLatch

import com.sksamuel.elastic4s.ElasticClient
import org.elasticsearch.common.settings.Settings
import org.slf4s.Logging
import velocorner.model.Activity
import velocorner.util.JsonIo

import scala.io.Source

case class ElasticDocument(id: Long, `type`: String, json: String)

object ElasticManual extends App with Logging {

  log.info("starting...")

  val settings = Settings.settingsBuilder()
    .put("http.enabled", true)
    .put("path.home", "c:/apps/elastic")
  val client = ElasticClient.local(settings.build)

  log.info("reading json entries...")
  val json = Source.fromURL(getClass.getResource("/data/strava/last10activities.json")).mkString
  val activities = JsonIo.read[List[Activity]](json)
  val docs = activities.map(a => ElasticDocument(a.id, a.`type`, JsonIo.write(a)))

  log.info("indexing...")

  val cl = new CountDownLatch(1)
  cl.await()
}