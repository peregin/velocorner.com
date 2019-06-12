package velocorner.search.manual

import com.sksamuel.elastic4s.ElasticDsl._
import org.slf4s.Logging
import velocorner.manual.{AwaitSupport, MyMacConfig}
import velocorner.search.ElasticSupport
import velocorner.storage.Storage

/**
  * Created by levi on 24.12.16.
  */
object ElasticFromStorageManual extends App with ElasticSupport with AwaitSupport with Logging with MyMacConfig {

  val storage = Storage.create("or") // re, co, mo, dy, or
  storage.initialize()
  log.info("initialized...")

  val activities = await(storage.listRecentActivities(432909, 10000))
  storage.destroy()

  val client = localCluster()

  log.info(s"indexing ${activities.size} documents ...")
  val indices = map2Indices(activities)
  client.execute(bulk(indices)).await
  log.info("done...")

  client.close()
}
