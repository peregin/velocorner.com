package velocorner.manual.storage

import java.util.concurrent.{CountDownLatch, Executors, ThreadFactory, TimeUnit}

import com.typesafe.scalalogging.LazyLogging
import velocorner.manual.{AggregateActivities, MyMacConfig}
import velocorner.model.strava.Activity
import velocorner.storage.Storage
import velocorner.util.{JsonIo, Metrics}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.control.Exception._

/**
  * Created by levi on 19.11.16.
  */
object StressApp extends App with Metrics with LazyLogging with AggregateActivities with MyMacConfig {

  val par = 10
  val latch = new CountDownLatch(par)
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(par, (r: Runnable) => {
    val t = new Thread(r, "worker")
    t.setDaemon(true)
    t
  }))

  val json = Source.fromURL(getClass.getResource("/data/strava/last30activities.json")).mkString
  val activities = JsonIo.read[List[Activity]](json)

  val storage = Storage.create("or") // mo
  storage.initialize()
  ultimately(storage.destroy()) {

    1 to par foreach { i =>
      Future {
        val activity = if (i % 2 == 0) {
          logger.info(s"start[$i] query activity...")
          storage.dailyProgressForAthlete(432909, "Ride")
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
