package velocorner.search

import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}
import com.sksamuel.elastic4s.http.JavaClient

trait ElasticSupport {

  // TODO: extract it to config
  private lazy val client = JavaClient(ElasticProperties("http://192.168.0.11:9200"))

  def localCluster(): ElasticClient = ElasticClient(client)

}
