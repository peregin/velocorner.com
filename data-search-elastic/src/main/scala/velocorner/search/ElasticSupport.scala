package velocorner.search

import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}
import com.sksamuel.elastic4s.http.JavaClient
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback
import velocorner.SecretConfig

trait ElasticSupport {

  def config: SecretConfig

  def createElasticClient(): ElasticClient = ElasticClient(
    JavaClient(
      props = ElasticProperties(config.getElasticSearchUrl),
      httpClientConfigCallback = new HttpClientConfigCallback {
        override def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder =
//          val provider = new BasicCredentialsProvider
//          val credentials = new UsernamePasswordCredentials(config.getZincUser, config.getZincPassword)
//          provider.setCredentials(AuthScope.ANY, credentials)
//          httpClientBuilder.setDefaultCredentialsProvider(provider)
          httpClientBuilder
      }
    )
  )
}
