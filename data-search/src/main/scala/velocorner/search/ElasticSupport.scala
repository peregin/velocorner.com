package velocorner.search

import com.sksamuel.elastic4s.{ElasticClient, ElasticNodeEndpoint, ElasticProperties}
import com.sksamuel.elastic4s.http.JavaClient

trait ElasticSupport {

  // TODO: extract it to config
  private lazy val client = JavaClient(ElasticProperties("http://elasticsearch:9200"))

  def localCluster(): ElasticClient = ElasticClient(client)

}
