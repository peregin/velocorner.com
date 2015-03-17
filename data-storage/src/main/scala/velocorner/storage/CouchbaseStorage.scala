package velocorner.storage

import java.net.URI
import java.util.concurrent.TimeUnit

import com.couchbase.client.CouchbaseClient
import collection.JavaConversions._


object CouchbaseStorage extends Storage {

  val uri = URI.create("http://localhost:8091/pools")
  lazy val client = new CouchbaseClient(List(uri), "default", "")

  def logStats() = {
      client.getStats.map { case (address, map) =>
        s"$address => ${map.filterKeys(_.startsWith("ep_db")).mkString("\n")}"
      }
  }

  def disconnect() {
    client.shutdown(3, TimeUnit.SECONDS)
  }
}
