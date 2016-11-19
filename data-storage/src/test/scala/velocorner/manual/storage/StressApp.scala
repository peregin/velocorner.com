package velocorner.manual.storage

import java.util.concurrent.{CountDownLatch, Executors, TimeUnit}

import org.slf4s.Logging
import velocorner.manual.{AggregateActivities, MyMacConfig}
import velocorner.storage.Storage
import velocorner.util.Metrics

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.Exception._

/**
  * Created by levi on 19.11.16.
  */
object StressApp extends App with Metrics with Logging with AggregateActivities with MyMacConfig {

  val par = 1
  val latch = new CountDownLatch(par)
  implicit var ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(par))

  val storage = Storage.create("or") // mo
  storage.initialize()
  ultimately(storage.destroy()) {

    1 to par foreach { i =>
      Future {
        log.info(s"query[$i] starting...")
        storage.dailyProgressForAll(100)
        log.info(s"query[$i] ran...")
        latch.countDown()
      }
    }

    // wait for all
    log.info("wait for the workers")
    latch.await(10, TimeUnit.SECONDS)
    log.info("done...")
  }
}
