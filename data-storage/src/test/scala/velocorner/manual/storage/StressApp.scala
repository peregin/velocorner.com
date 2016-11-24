package velocorner.manual.storage

import java.util.concurrent.{CountDownLatch, Executors, ThreadFactory, TimeUnit}

import org.slf4s.Logging
import velocorner.manual.{AggregateActivities, MyMacConfig}
import velocorner.model.Activity
import velocorner.storage.Storage
import velocorner.util.{JsonIo, Metrics}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.control.Exception._

/**
  * Created by levi on 19.11.16.
  */
object StressApp extends App with Metrics with Logging with AggregateActivities with MyMacConfig {

  val par = 10
  val latch = new CountDownLatch(par)
  implicit var ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(par, new ThreadFactory {
    override def newThread(r: Runnable): Thread = {
      val t = new Thread(r, "worker")
      t.setDaemon(true)
      t
    }
  }))

  val json = Source.fromURL(getClass.getResource("/data/strava/last30activities.json")).mkString
  val activities = JsonIo.read[List[Activity]](json)

  val storage = Storage.create("or") // mo
  storage.initialize()
  ultimately(storage.destroy()) {

    1 to par foreach { i =>
      Future {
        val activity = if (i % 2 == 0) {
          log.info(s"start[$i] query activity...")
          storage.dailyProgressForAll(100)
          "query"
        } else {
          log.info(s"start[$i] store activity...")
          storage.store(activities)
          "store"
        }
        log.info(s"done[$i] $activity activity...")
        latch.countDown()
      }
    }

    // wait for all
    log.info("wait for the workers")
    latch.await(10, TimeUnit.SECONDS)
    log.info("done...")
  }
}
