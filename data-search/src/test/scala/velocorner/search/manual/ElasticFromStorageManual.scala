package velocorner.search.manual

import com.sksamuel.elastic4s.ElasticDsl._
import com.typesafe.scalalogging.LazyLogging
import velocorner.manual.{AwaitSupport, MyMacConfig}
import velocorner.search.ElasticSupport
import velocorner.storage.Storage

/**
  * Created by levi on 24.12.16.
  */
object ElasticFromStorageManual extends App with ElasticSupport with AwaitSupport with LazyLogging with MyMacConfig {

  val storage = Storage.create("or") // re, co, mo, dy, or
  storage.initialize()
  logger.info("initialized...")

  val activities = await(storage.listRecentActivities(432909, 10000))
  storage.destroy()

  val client = localCluster()

  logger.info(s"indexing ${activities.size} documents ...")
  val indices = map2Indices(activities)
  client.execute(bulk(indices)).await
  logger.info("done...")

  client.close()
}
