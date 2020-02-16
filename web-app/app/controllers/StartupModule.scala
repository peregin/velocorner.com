package controllers

import com.google.inject.AbstractModule

class StartupModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[StartupService]).asEagerSingleton
  }
}
