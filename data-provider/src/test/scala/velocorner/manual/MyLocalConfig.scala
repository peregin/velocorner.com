package velocorner.manual

import zio.ZLayer
import zio.clock.Clock
import zio.console.Console
import zio.logging.{LogLevel, Logging}

/**
  * Created by levi on 28/11/15.
  */
trait MyLocalConfig {

  // the property file having the application secrets, strava token, bucket password, etc.
  sys.props += "config.file" -> "/Users/levi/Downloads/velo/velocorner/local.conf"

  lazy val zEnv: ZLayer[Console with Clock, Nothing, Logging] = Logging.console(logLevel = LogLevel.Debug, format = (_, logEntry) => logEntry)
}
