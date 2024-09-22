package controllers

import com.google.inject.AbstractModule
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Singleton
import velocorner.util.Metrics

class StartupModule extends AbstractModule {

  override def configure(): Unit =
    bind(classOf[StartupService]).asEagerSingleton()
}

object StartupService {

  private val startupTime = System.currentTimeMillis()
  def elapsedTimeText(): String = Metrics.elapsedTimeText(System.currentTimeMillis() - startupTime)
}

@Singleton
class StartupService extends LazyLogging {

  logger.info(s"startup service initializing at ${StartupService.startupTime} ...")
}
