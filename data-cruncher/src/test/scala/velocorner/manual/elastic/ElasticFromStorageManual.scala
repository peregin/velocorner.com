package velocorner.manual.elastic

import com.sksamuel.elastic4s.ElasticDsl.{bulk, index}
import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}
import org.elasticsearch.common.settings.Settings
import org.slf4s.Logging
import velocorner.storage.Storage
import com.sksamuel.elastic4s.ElasticDsl._
import velocorner.manual.MyMacConfig

/**
  * Created by levi on 24.12.16.
  */
object ElasticFromStorageManual extends App with Logging with MyMacConfig {

  val storage = Storage.create("or") // re, co, mo, dy, or
  storage.initialize()
  log.info("initialized...")

  val activities = storage.listRecentActivities(432909, 10000)
  storage.destroy()

  val remoteSettings = Settings.builder().put("cluster.name", "peregin")
  val client = ElasticClient.transport(remoteSettings.build(), ElasticsearchClientUri("elasticsearch://localhost:9300"))

  log.info(s"indexing ${activities.size} documents ...")
  val indices = activities.map(a => index into s"velocorner/${a.`type`}"
    fields(
    "name" -> a.name,
    "start_date" -> a.start_date,
    "distance" -> a.distance / 1000,
    "elevation" -> a.total_elevation_gain,
    "average_speed" -> a.average_speed.getOrElse(0f),
    "max_speed" -> a.max_speed.getOrElse(0f),
    "average_temp" -> a.average_temp.getOrElse(0f),
    "average_watts" -> a.average_watts.getOrElse(0f)
  ) id a.id)
  client.execute(bulk(indices)).await
  log.info("done...")

}
