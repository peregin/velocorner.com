package velocorner.manual.elastic

import java.util.concurrent.CountDownLatch

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import org.elasticsearch.common.settings.Settings
import org.slf4s.Logging
import velocorner.model.Activity
import velocorner.util.JsonIo

import scala.io.Source


object ElasticManual extends App with Logging {

  log.info("starting...")

  val settings = Settings.builder()
    .put("http.enabled", true)
    .put("path.home", "c:/apps/elastic")
  val client = ElasticClient.local(settings.build)

  log.info("reading json entries...")
  val json = Source.fromURL(getClass.getResource("/data/strava/last30activities.json")).mkString
  val activities = JsonIo.read[List[Activity]](json)

  log.info(s"indexing ${activities.size} documents ...")
  val indices = activities.map(a => index into s"velocorner/${a.`type`}" fields("name" -> a.name) id a.id)
  client.execute(bulk(indices)).await

  log.info("searching...")
  val res = client.execute(search in "velocorner"->"Ride" query "Uetli" limit 5).await
  log.info(s"found $res")

  log.info("counting...")
  val cres = client.execute(search("velocorner").size(0)).await
  log.info(s"found ${cres.totalHits}")


  //val cl = new CountDownLatch(1)
  //cl.await()
}