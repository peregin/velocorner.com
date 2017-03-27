package velocorner.util

import com.sksamuel.elastic4s.ElasticDsl.index
import com.sksamuel.elastic4s.indexes.IndexDefinition
import com.sksamuel.elastic4s.{ElasticsearchClientUri, TcpClient}
import org.elasticsearch.common.settings.Settings
import velocorner.model.Activity

/**
  * Created by levi on 21.03.17.
  */
trait ElasticSupport {

  //val settings = Settings.builder()
  //  .put("http.enabled", true)
  //  .put("path.home", "elastic")
  //val client = ElasticClient.local(settings.build)
  def elasticLocal() = {
    val remoteSettings = Settings.builder().put("cluster.name", "peregin")
    TcpClient.transport(remoteSettings.build(), ElasticsearchClientUri("elasticsearch://localhost:9300"))
  }

  def map2Indices(activities: Iterable[Activity]): Iterable[IndexDefinition] = {
    activities.map { a =>
      val ixDefinition = index into s"velocorner/${a.`type`}"
      extractIndices(a, ixDefinition) id a.id
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
