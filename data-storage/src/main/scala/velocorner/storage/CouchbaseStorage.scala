package velocorner.storage

import java.net.URI
import java.util.concurrent.TimeUnit

import com.couchbase.client.CouchbaseClient
import velocorner.model.Activity
import velocorner.util.JsonIo
import collection.JavaConversions._


class CouchbaseStorage(password: String) extends Storage {

  lazy val uri = URI.create("http://localhost:8091/pools")
  lazy val client = new CouchbaseClient(List(uri), "velocorner", password)

  override def store(activities: List[Activity]) {
    activities.foreach{a =>
      client.add(a.id.toString, 0, JsonIo.write(a))
    }
  }

  // initializes any connections, pools, resources needed to open a storage session
  override def initialize() {
  }

  // releases any connections, resources used
  override def destroy() {
    client.shutdown(3, TimeUnit.SECONDS)
  }
}
