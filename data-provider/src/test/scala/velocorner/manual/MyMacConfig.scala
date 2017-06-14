package velocorner.manual

/**
  * Created by levi on 28/11/15.
  */
trait MyMacConfig {

  // the property file having the application secrets, strava token, bucket password, it is not part of the git project
  sys.props += "config.file" -> "/Users/levi/Downloads/velo/velocorner.conf"
}
