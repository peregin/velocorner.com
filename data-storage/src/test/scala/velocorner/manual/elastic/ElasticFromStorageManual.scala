package velocorner.manual.elastic

import com.sksamuel.elastic4s.ElasticDsl.{bulk, index}
import com.sksamuel.elastic4s.{ElasticsearchClientUri, TcpClient}
import org.elasticsearch.common.settings.Settings
import org.slf4s.Logging
import velocorner.storage.Storage
import com.sksamuel.elastic4s.ElasticDsl._
import velocorner.manual.MyMacConfig
import velocorner.util.ElasticSupport

/**
  * Created by levi on 24.12.16.
  */
object ElasticFromStorageManual extends App with ElasticSupport with Logging with MyMacConfig {

  val storage = Storage.create("or") // re, co, mo, dy, or
  storage.initialize()
  log.info("initialized...")

  val activities = storage.listRecentActivities(432909, 10000)
  storage.destroy()

  val remoteSettings = Settings.builder().put("cluster.name", "peregin")
  val client = TcpClient.transport(remoteSettings.build(), ElasticsearchClientUri("elasticsearch://localhost:9300"))

  log.info(s"indexing ${activities.size} documents ...")
  val indices = activities.map{a =>
    val ixDef = index into s"velocorner/${a.`type`}"
    extractIndices(a, ixDef) id a.id
  }
  client.execute(bulk(indices)).await
  log.info("done...")

}
