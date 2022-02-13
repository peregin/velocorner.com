package velocorner.search

import com.sksamuel.elastic4s.{ElasticClient, ElasticNodeEndpoint, ElasticProperties}
import com.sksamuel.elastic4s.http.JavaClient

trait ElasticSupport {

  private lazy val client = JavaClient(ElasticProperties(Seq(ElasticNodeEndpoint("http", "localhost", 9200, prefix = None))))

  def localCluster(): ElasticClient = ElasticClient(client)

}
