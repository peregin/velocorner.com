package velocorner.search

import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}
import com.sksamuel.elastic4s.http.JavaClient

trait ElasticSupport {

  def elasticUrl(): String

  def createElasticClient(): ElasticClient = ElasticClient(JavaClient(ElasticProperties(elasticUrl())))
}
