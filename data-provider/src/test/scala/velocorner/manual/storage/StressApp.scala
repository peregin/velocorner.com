package velocorner.manual.storage

import java.util.concurrent.{CountDownLatch, Executors, TimeUnit}
import com.typesafe.scalalogging.LazyLogging
import velocorner.manual.{AggregateActivities, MyLocalConfig}
import velocorner.storage.Storage
import velocorner.util.{CloseableResource, JsonIo}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.control.Exception._
import mouse.all._
import velocorner.api.strava.Activity

/**
 * Created by levi on 19.11.16.
 */
object StressApp extends App with CloseableResource with LazyLogging with AggregateActivities with MyLocalConfig {

  val par = 10
  val latch = new CountDownLatch(par)
  implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(par, (r: Runnable) => new Thread(r, "worker") <| (_.setDaemon(true))))

  val json = withCloseable(Source.fromURL(getClass.getResource("/data/strava/last30activities.json")))(_.mkString)
  val activities = JsonIo.read[List[Activity]](json)

  val storage = Storage.create("or") // mo
  storage.initialize()
  ultimately(storage.destroy()) {

    1 to par foreach { i =>
      Future {
        val activity = if (i % 2 == 0) {
          logger.info(s"start[$i] query activity...")
          storage.listAllActivities(432909, "Ride")
          "query"
        } else {
          logger.info(s"start[$i] store activity...")
          storage.storeActivity(activities)
          "store"
        }
        logger.info(s"done[$i] $activity activity...")
        latch.countDown()
      }
    }

    // wait for all
    logger.info("wait for the workers")
    latch.await(10, TimeUnit.SECONDS)
    logger.info("done...")
  }
}
