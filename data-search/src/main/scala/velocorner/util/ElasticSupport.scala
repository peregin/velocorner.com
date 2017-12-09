package velocorner.util

import com.sksamuel.elastic4s.indexes.{IndexApi, IndexDefinition}
import com.sksamuel.elastic4s.{ElasticsearchClientUri, TcpClient}
import org.elasticsearch.common.settings.Settings
import velocorner.model.Activity

/**
  * Created by levi on 21.03.17.
  */
trait ElasticSupport extends IndexApi {

//  def elasticEmbedded() = {
//    val localSettings = Settings.builder()
//      .put("http.enabled", true)
//      .put("path.home", "elastic")
//      .put("path.data", "elastic/data")
//      .put("path.repo", "elastic/repo")
//      .put("cluster.name", "velocorner")
//    val node = LocalNode(localSettings.build())
//    node.elastic4sclient(shutdownNodeOnClose = true)
//  }

  def elasticCluster() = {
    val remoteSettings = Settings.builder()
      .put("cluster.name", "peregin")
    TcpClient.transport(remoteSettings.build(), ElasticsearchClientUri("elasticsearch://localhost:9300"))
  }

  def map2Indices(activities: Iterable[Activity]): Iterable[IndexDefinition] = {
    activities.map { a =>
      val ixDefinition = indexInto(s"velocorner/${a.`type`}")
      extractIndices(a, ixDefinition).withId(a.id.toString)
    }
  }

  def extractIndices(a: Activity, id: IndexDefinition): IndexDefinition = id.fields(
    "name" -> a.name,
    "start_date" -> a.start_date,
    "distance" -> a.distance / 1000,
    "elevation" -> a.total_elevation_gain,
    "average_speed" -> a.average_speed.getOrElse(0f),
    "max_speed" -> a.max_speed.getOrElse(0f),
    "average_temp" -> a.average_temp.getOrElse(0f),
    "average_watts" -> a.average_watts.getOrElse(0f)
  )
}
