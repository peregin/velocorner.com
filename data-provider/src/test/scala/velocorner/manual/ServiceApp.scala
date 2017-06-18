package velocorner.manual

import org.slf4s.Logging
import rx.lang.scala.Observable

/**
  * Created by levi on 14.06.17.
  */
object ServiceApp extends App with Logging {

  log.info("service starting...")

  val obs1 = Observable.just(1, 5, 8)
  val obs2 = Observable.just(2, 7, 10)
  val obs3 = obs1.merge(obs2)
  obs3.subscribe(i => println(i))
}
