package velocorner.manual.elastic

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ElasticsearchClientUri, TcpClient}
import org.elasticsearch.common.settings.Settings
import org.slf4s.Logging
import velocorner.model.Activity
import velocorner.util.{ElasticSupport, JsonIo}

import scala.io.Source


object ElasticManual extends App with ElasticSupport with Logging {

  log.info("starting...")

  //val settings = Settings.builder()
  //  .put("http.enabled", true)
  //  .put("path.home", "elastic")
  //val client = ElasticClient.local(settings.build)
  val remoteSettings = Settings.builder().put("cluster.name", "peregin")
  val client = TcpClient.transport(remoteSettings.build(), ElasticsearchClientUri("elasticsearch://localhost:9300"))

  log.info("reading json entries...")
  val json = Source.fromURL(getClass.getResource("/data/strava/last30activities.json")).mkString
  val activities1 = JsonIo.read[List[Activity]](json)
  val activities = read("last30activities.json", "activity-805296924.json")

  log.info(s"indexing ${activities.size} documents ...")
  val indices = activities.map{a =>
    val ixDef = index into s"velocorner/${a.`type`}"
    extractIndices(a, ixDef) id a.id
  }
  client.execute(bulk(indices)).await

  log.info("searching...")
  val res = client.execute(search in "velocorner"->"Ride" query "Uetli" limit 5).await
  log.info(s"found $res")
  res.original.getHits.getHits.headOption.foreach{first =>
    log.info(s"first entry: $first")
  }

  //log.info("counting...")
  //val cres = client.execute(search("velocorner").size(0)).await
  //log.info(s"found ${cres.totalHits}")

  //client.close()

  def read(name: String*): Seq[Activity] = {
    name.map{ resourceName =>
      val json = Source.fromURL(getClass.getResource(s"/data/strava/$resourceName")).mkString
      JsonIo.read[List[Activity]](json)
    }.flatten
  }
}